package gui;

import log.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LogWindow extends JInternalFrame implements LogChangeListener {
    private final LogWindowSource logSource;
    private final JTextArea logContent;
    private final Timer updateTimer;
    private LogLevel currentFilterLevel = LogLevel.Trace; // По умолчанию показываем все уровни

    public LogWindow(LogWindowSource logSource) {
        super("Протокол работы", true, true, true, true);
        this.logSource = logSource;
        this.logSource.registerListener(this);

        logContent = new JTextArea();
        logContent.setEditable(false);
        logContent.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logContent);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Панель с фильтрами и кнопками
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 1. Фильтр по уровню логирования
        JComboBox<LogLevel> levelFilter = new JComboBox<>(LogLevel.values());
        levelFilter.setSelectedItem(LogLevel.Trace);
        levelFilter.addActionListener(e -> {
            currentFilterLevel = (LogLevel) levelFilter.getSelectedItem();
            updateLogContent();
        });
        controlPanel.add(new JLabel("Уровень:"));
        controlPanel.add(levelFilter);

        // 2. Кнопка очистки
        JButton clearButton = new JButton("Очистить");
        clearButton.addActionListener(e -> logContent.setText(""));
        controlPanel.add(clearButton);

        // 3. Кнопка сохранения в файл
        JButton saveButton = new JButton("Сохранить в файл");
        saveButton.addActionListener(this::saveLogToFile);
        controlPanel.add(saveButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        getContentPane().add(mainPanel);
        pack();

        updateTimer = new Timer(100, e -> updateLogContent());
        updateTimer.setRepeats(false);
        updateLogContent();
    }

    private void updateLogContent() {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : logSource.all()) {
            if (entry.getLevel().level() >= currentFilterLevel.level()) {
                content.append(String.format("[%s] %s%n",
                        entry.getLevel().name(), entry.getMessage()));
            }
        }
        logContent.setText(content.toString());
        logContent.setCaretPosition(logContent.getDocument().getLength());
    }

    private void saveLogToFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить лог как...");
        fileChooser.setSelectedFile(new File("robot_log.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                for (LogEntry entry : logSource.all()) {
                    writer.printf("[%s] %s%n",
                            entry.getLevel().name(), entry.getMessage());
                }
                Logger.debug("Лог сохранён в: " + file.getAbsolutePath());
            } catch (IOException ex) {
                Logger.error("Ошибка сохранения лога: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Ошибка при сохранении файла",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
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