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
    private static final Dimension NORMAL_SIZE = new Dimension(950, 850);

    public WindowManager(JDesktopPane desktopPane, RobotModel robotModel) {
        this.desktopPane = desktopPane;
        this.robotModel = robotModel;
    }

    public void initializeWindows() {
        // Удаляем все существующие окна
        desktopPane.removeAll();

        // Создаем окно логов
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setTitle("Протокол работы");
        logWindow.setBounds(10, 10, 500, 500);
        addWindow(logWindow);

        // Создаем игровое поле
        GameVisualizer visualizer = new GameVisualizer(robotModel);
        GameWindow gameWindow = new GameWindow(visualizer);
        gameWindow.setTitle("Игровое поле");
        gameWindow.setBounds(520, 10, 400, 400);
        addWindow(gameWindow);

        // Создаем окно координат
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
            e.printStackTrace();
        }
    }

    public void saveWindowState(JFrame frame) {
        Properties props = new Properties();
        int state = frame.getExtendedState();
        props.setProperty("main.state", String.valueOf(state));

        if (state == Frame.NORMAL) {
            // Убедимся, что размеры не меньше NORMAL_SIZE
            int width = Math.max(frame.getWidth(), NORMAL_SIZE.width);
            int height = Math.max(frame.getHeight(), NORMAL_SIZE.height);
            props.setProperty("main.x", String.valueOf(frame.getX()));
            props.setProperty("main.y", String.valueOf(frame.getY()));
            props.setProperty("main.width", String.valueOf(width));
            props.setProperty("main.height", String.valueOf(height));
        }

        for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
            saveInternalFrameState(internalFrame, internalFrame.getTitle(), props);
        }

        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            props.store(fos, "Window State Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadWindowState(JFrame frame) {
        Properties props = new Properties();
        File configFile = new File(CONFIG_PATH);

        // Устанавливаем минимальный размер
        frame.setMinimumSize(NORMAL_SIZE);

        // Устанавливаем слушатель для центрирования при сворачивании
        frame.addWindowStateListener(e -> {
            int newState = e.getNewState();
            int oldState = e.getOldState();
            if ((newState & Frame.NORMAL) != 0 && (oldState & Frame.MAXIMIZED_BOTH) != 0) {
                centerWindow(frame);
            }
        });

        // Отслеживаем перемещение и изменение размеров окна
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                if (frame.getExtendedState() == Frame.NORMAL) {
                    props.setProperty("main.x", String.valueOf(frame.getX()));
                    props.setProperty("main.y", String.valueOf(frame.getY()));
                }
            }

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (frame.getExtendedState() == Frame.NORMAL) {
                    props.setProperty("main.width", String.valueOf(Math.max(frame.getWidth(), NORMAL_SIZE.width)));
                    props.setProperty("main.height", String.valueOf(Math.max(frame.getHeight(), NORMAL_SIZE.height)));
                }
            }
        });

        if (!configFile.exists()) {
            // При первом запуске окно развернуто
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            return;
        }

        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            props.load(fis);

            int state = Integer.parseInt(props.getProperty("main.state", String.valueOf(Frame.MAXIMIZED_BOTH)));
            if (state == Frame.NORMAL) {
                int x = Integer.parseInt(props.getProperty("main.x", "-1"));
                int y = Integer.parseInt(props.getProperty("main.y", "-1"));
                int width = Integer.parseInt(props.getProperty("main.width", String.valueOf(NORMAL_SIZE.width)));
                int height = Integer.parseInt(props.getProperty("main.height", String.valueOf(NORMAL_SIZE.height)));

                // Проверяем, чтобы размеры были не меньше NORMAL_SIZE
                width = Math.max(width, NORMAL_SIZE.width);
                height = Math.max(height, NORMAL_SIZE.height);

                // Если координаты некорректны, центрируем окно
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                if (x < 0 || y < 0 || x > screenSize.width - width || y > screenSize.height - height) {
                    centerWindow(frame);
                } else {
                    frame.setBounds(x, y, width, height);
                }
            }
            frame.setExtendedState(state);

            for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
                loadInternalFrameState(internalFrame, internalFrame.getTitle(), props);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            frame.setExtendedState(Frame.MAXIMIZED_BOTH); // По умолчанию развернуто
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
            e.printStackTrace();
        }
    }

    private void centerWindow(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = NORMAL_SIZE.width;
        int height = NORMAL_SIZE.height;
        int x = (screenSize.width - width) / 2;
        int y = (screenSize.height - height) / 2;
        frame.setBounds(x, y, width, height);
    }

    public void shutdown() {
        saveWindowState(desktopPane.getTopLevelAncestor() instanceof JFrame ? (JFrame) desktopPane.getTopLevelAncestor() : null);
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof GameWindow) {
                ((GameWindow) frame).shutdown();
            }
            frame.dispose();
        }
    }
}