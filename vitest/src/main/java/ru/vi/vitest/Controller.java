package ru.vi.vitest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.*;

public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private final Model model;
    private final View view;
    private final ExecutorService service;
    private final BlockingQueue<String> commands = new ArrayBlockingQueue<>(10);

    public Controller(Model model, View view, int workers) {
        this.model = model;
        this.view = view;
        this.service = Executors.newFixedThreadPool(workers);

        for (int i = 0; i < workers; ++i) {
            service.submit(new Worker(this, commands));
        }
    }

    public Model getModel() {
        return model;
    }

    public View getView() {
        return view;
    }

    public void run() {
        view.btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = Integer.parseInt(view.txtN.getText());
                for (int i = 0; i < n; ++i) {
                    commands.add(Worker.ADD);
                }
                log.info("model.size=" + model.size());
                save();
            }
        });

        view.btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = Integer.parseInt(view.txtN.getText());
                for (int i = 0; i < n; ++i) {
                    commands.add(Worker.REMOVE);
                }
                log.info("model.size=" + model.size());
                save();
            }
        });

        view.run();

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                view.lblTime.setText(String.valueOf(System.currentTimeMillis()));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void save() {
        JProgressBar bar = view.createProgressBar(model.size());
        try {
            commands.add(Worker.SAVE);
            // TODO: update progress bar
        } finally {
            view.releaseProgressBar(bar);
        }
    }
}
