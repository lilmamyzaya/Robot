package gui;

import model.RobotModel;
import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class WindowManager {
    private final JDesktopPane desktopPane;
    private final RobotModel robotModel;
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/robots_config.properties";
    private static final Dimension NORMAL_SIZE = new Dimension(950, 850); // Нормальный размер окна

    public WindowManager(JDesktopPane desktopPane, RobotModel robotModel) {
        this.desktopPane = desktopPane;
        this.robotModel = robotModel;
    }

    public void initializeWindows() {
        desktopPane.removeAll();

        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setTitle("Протокол работы");
        logWindow.setBounds(10, 10, 500, 500);
        addWindow(logWindow);

        GameVisualizer visualizer = new GameVisualizer(robotModel);
        GameWindow gameWindow = new GameWindow(visualizer);
        gameWindow.setTitle("Игровое поле");
        gameWindow.setBounds(520, 10, 400, 400);
        addWindow(gameWindow);

        RobotCoordinatesWindow coordsWindow = new RobotCoordinatesWindow(robotModel);
        coordsWindow.setTitle("Координаты робота");
        coordsWindow.setBounds(930, 10, 200, 100);
        addWindow(coordsWindow);
    }

    public void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            Logger.error("Ошибка при выборе окна: " + e.getMessage());
        }
    }

    public void saveWindowState(JFrame frame) {
        Properties props = new Properties();
        int state = frame.getExtendedState();
        props.setProperty("main.state", String.valueOf(state));

        if (state == Frame.NORMAL) {
            int width = Math.max(frame.getWidth(), NORMAL_SIZE.width);
            int height = Math.max(frame.getHeight(), NORMAL_SIZE.height);
            props.setProperty("main.x", String.valueOf(frame.getX()));
            props.setProperty("main.y", String.valueOf(frame.getY()));
            props.setProperty("main.width", String.valueOf(width));
            props.setProperty("main.height", String.valueOf(height));
            Logger.debug("Сохранено состояние NORMAL: x=" + frame.getX() + ", y=" + frame.getY() + ", width=" + width + ", height=" + height);
        } else {
            Logger.debug("Сохранено состояние: state=" + state);
        }

        for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
            saveInternalFrameState(internalFrame, internalFrame.getTitle(), props);
        }

        try {
            File configFile = new File(CONFIG_PATH);
            configFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "Window State Configuration");
                Logger.debug("Конфигурация сохранена в " + CONFIG_PATH);
            }
        } catch (IOException e) {
            Logger.error("Ошибка сохранения состояния окна: " + e.getMessage());
        }
    }

    public void loadWindowState(JFrame frame) {
        Properties props = new Properties();
        File configFile = new File(CONFIG_PATH);

        frame.setMinimumSize(NORMAL_SIZE);

        // Слушатель для центрирования при восстановлении из развёрнутого или свёрнутого состояния
        frame.addWindowStateListener(e -> {
            int oldState = e.getOldState();
            int newState = e.getNewState();
            Logger.debug("Состояние окна изменилось: старое=" + oldState + ", новое=" + newState);
            if ((newState == Frame.NORMAL) &&
                    (oldState == Frame.MAXIMIZED_BOTH || oldState == Frame.ICONIFIED)) {
                frame.setSize(NORMAL_SIZE);
                centerWindow(frame);
                SwingUtilities.invokeLater(() -> saveWindowState(frame));
            }
        });

        if (!configFile.exists()) {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            Logger.debug("Первый запуск: окно развёрнуто на весь экран");
            return;
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);

            int state = Integer.parseInt(props.getProperty("main.state", String.valueOf(Frame.MAXIMIZED_BOTH)));
            Logger.debug("Загружено состояние окна: state=" + state);
            frame.setExtendedState(state);

            if (state == Frame.NORMAL) {
                int x = Integer.parseInt(props.getProperty("main.x", "-1"));
                int y = Integer.parseInt(props.getProperty("main.y", "-1"));
                int width = Integer.parseInt(props.getProperty("main.width", String.valueOf(NORMAL_SIZE.width)));
                int height = Integer.parseInt(props.getProperty("main.height", String.valueOf(NORMAL_SIZE.height)));

                width = Math.max(width, NORMAL_SIZE.width);
                height = Math.max(height, NORMAL_SIZE.height);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                // Если координаты выходят за пределы экрана, центрируем окно
                if (x < 0 || y < 0 || x + width > screenSize.width || y + height > screenSize.height) {
                    Logger.debug("Некорректные координаты (x=" + x + ", y=" + y + "), центрируем окно");
                    centerWindow(frame);
                } else {
                    frame.setBounds(x, y, width, height);
                    Logger.debug("Установлены сохранённые координаты: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
                }
            }

            for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
                loadInternalFrameState(internalFrame, internalFrame.getTitle(), props);
            }
        } catch (IOException | NumberFormatException e) {
            Logger.error("Ошибка загрузки состояния окна: " + e.getMessage());
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            Logger.debug("Ошибка загрузки, окно развёрнуто на весь экран");
        }
    }

    private void saveInternalFrameState(JInternalFrame frame, String name, Properties props) {
        props.setProperty(name + ".x", String.valueOf(frame.getX()));
        props.setProperty(name + ".y", String.valueOf(frame.getY()));
        props.setProperty(name + ".width", String.valueOf(frame.getWidth()));
        props.setProperty(name + ".height", String.valueOf(frame.getHeight()));
        props.setProperty(name + ".icon", String.valueOf(frame.isIcon()));
    }

    private void loadInternalFrameState(JInternalFrame frame, String name, Properties props) {
        try {
            int x = Integer.parseInt(props.getProperty(name + ".x", "50"));
            int y = Integer.parseInt(props.getProperty(name + ".y", "50"));
            int width = Integer.parseInt(props.getProperty(name + ".width", "300"));
            int height = Integer.parseInt(props.getProperty(name + ".height", "300"));
            boolean icon = Boolean.parseBoolean(props.getProperty(name + ".icon", "false"));

            frame.setBounds(x, y, width, height);
            frame.setIcon(icon);
        } catch (Exception e) {
            Logger.error("Ошибка загрузки состояния внутреннего окна: " + e.getMessage());
        }
    }

    private void centerWindow(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
        Logger.debug("Центрирование окна: x=" + x + ", y=" + y);
    }

    public void shutdown() {
        JFrame mainFrame = desktopPane.getTopLevelAncestor() instanceof JFrame ? (JFrame) desktopPane.getTopLevelAncestor() : null;
        if (mainFrame != null) {
            saveWindowState(mainFrame);
        }
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof GameWindow) {
                ((GameWindow) frame).shutdown();
            }
            frame.dispose();
        }
    }
}