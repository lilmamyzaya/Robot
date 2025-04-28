package gui;

import log.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogWindow extends JInternalFrame implements LogChangeListener {
    private final LogWindowSource logSource;
    private final JTextArea logContent;
    private final Timer updateTimer;

    public LogWindow(LogWindowSource logSource) {
        super("Протокол работы", true, true, true, true);
        this.logSource = logSource;
        this.logSource.registerListener(this);

        logContent = new JTextArea();
        logContent.setEditable(false);
        logContent.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logContent);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        // Кнопка очистки лога
        JButton clearButton = new JButton("Очистить");
        clearButton.addActionListener(e -> {
            synchronized (logSource) {
                logContent.setText("");
            }
        });
        panel.add(clearButton, BorderLayout.SOUTH);

        getContentPane().add(panel);
        pack();

        // Таймер для отложенного обновления (чтобы избежать частых перерисовок)
        updateTimer = new Timer(100, e -> updateLogContent());
        updateTimer.setRepeats(false);
        updateLogContent();
    }

    private void updateLogContent() {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : logSource.all()) {
            content.append(String.format("[%s] %s%n",
                    entry.getLevel().name(), entry.getMessage()));
        }
        logContent.setText(content.toString());
        logContent.setCaretPosition(logContent.getDocument().getLength());
    }

    @Override
    public void onLogChanged() {
        if (!updateTimer.isRunning()) {
            updateTimer.start();
        }
    }

    @Override
    public void dispose() {
        logSource.unregisterListener(this);
        updateTimer.stop();
        super.dispose();
    }
}