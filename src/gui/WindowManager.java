package gui;

import model.RobotModel;
import log.Logger;

import javax.swing.*;
import java.io.*;
import java.util.Properties;


public class WindowManager {
    private final JDesktopPane desktopPane;
    private final RobotModel robotModel;
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/robots_config.properties";

    public WindowManager(JDesktopPane desktopPane, RobotModel robotModel) {
        this.desktopPane = desktopPane;
        this.robotModel = robotModel;
    }

    public void initializeWindows() {
        // Всегда создаем все окна
        createAllWindows();

        // Загружаем сохраненное состояние, если есть
        File configFile = new File(CONFIG_PATH);
        if (configFile.exists()) {
            loadWindowState();
        }
    }

    private void createAllWindows() {
        // Удаляем все существующие окна
        desktopPane.removeAll();

        // Создаем окно логов
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setTitle("Протокол работы");
        logWindow.setBounds(10, 10, 500, 500); // Фиксированная позиция по умолчанию
        addWindow(logWindow);

        // Создаем игровое поле
        GameVisualizer visualizer = new GameVisualizer(robotModel);
        GameWindow gameWindow = new GameWindow(visualizer);
        gameWindow.setTitle("Игровое поле");
        gameWindow.setBounds(520, 10, 400, 400); // Фиксированная позиция по умолчанию
        addWindow(gameWindow);

        // Создаем окно координат
        RobotCoordinatesWindow coordsWindow = new RobotCoordinatesWindow(robotModel);
        coordsWindow.setTitle("Координаты робота");
        coordsWindow.setBounds(930, 10, 200, 100); // Фиксированная позиция по умолчанию
        addWindow(coordsWindow);
    }

    public void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public void saveWindowState() {
        Properties props = new Properties();

        // Сохраняем состояние всех открытых окон
        for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
            saveInternalFrameState(internalFrame, internalFrame.getTitle(), props);
        }

        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            props.store(fos, "Window State Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadWindowState() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            props.load(fis);

            // Для каждого окна пытаемся загрузить сохраненное состояние
            for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
                String title = internalFrame.getTitle();
                if (props.containsKey(title + ".x")) {
                    // Если есть сохраненное состояние - восстанавливаем
                    loadInternalFrameState(internalFrame, title, props);
                }
                // Если нет сохраненного состояния - остается с фиксированной позицией
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveInternalFrameState(JInternalFrame frame, String name, Properties props) {
        props.setProperty(name + ".x", String.valueOf(frame.getX()));
        props.setProperty(name + ".y", String.valueOf(frame.getY()));
        props.setProperty(name + ".width", String.valueOf(frame.getWidth()));
        props.setProperty(name + ".height", String.valueOf(frame.getHeight()));
        props.setProperty(name + ".visible", String.valueOf(frame.isVisible()));
    }

    private void loadInternalFrameState(JInternalFrame frame, String name, Properties props) {
        try {
            int x = Integer.parseInt(props.getProperty(name + ".x"));
            int y = Integer.parseInt(props.getProperty(name + ".y"));
            int width = Integer.parseInt(props.getProperty(name + ".width"));
            int height = Integer.parseInt(props.getProperty(name + ".height"));
            boolean visible = Boolean.parseBoolean(props.getProperty(name + ".visible", "true"));

            frame.setBounds(x, y, width, height);
            frame.setVisible(visible);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        saveWindowState();

        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof GameWindow) {
                ((GameWindow) frame).shutdown();
            }
            frame.dispose();
        }
    }
}