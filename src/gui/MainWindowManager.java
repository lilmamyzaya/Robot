package gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class MainWindowManager {
    private final JFrame mainFrame;
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/robots_config.properties";
    private static final Dimension NORMAL_SIZE = new Dimension(950, 850);
    private boolean isMaximized = true;

    public MainWindowManager(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        setupWindowBehavior();
    }

    private void setupWindowBehavior() {
        mainFrame.setMinimumSize(NORMAL_SIZE);

        mainFrame.addWindowStateListener(e -> {
            // Только для главного окна
            if ((e.getNewState() & Frame.MAXIMIZED_BOTH) != 0) {
                isMaximized = true;
            } else if ((e.getNewState() & Frame.NORMAL) != 0) {
                isMaximized = false;
                if (e.getOldState() == Frame.ICONIFIED) {
                    centerWindow();
                }
            }
        });
    }

    public void initialize() {
        loadWindowState();

        if (isMaximized) {
            mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        } else {
            mainFrame.setExtendedState(Frame.NORMAL);
            centerWindow();
        }

        mainFrame.setVisible(true);
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - NORMAL_SIZE.width) / 2;
        int y = (screenSize.height - NORMAL_SIZE.height) / 2;
        mainFrame.setBounds(x, y, NORMAL_SIZE.width, NORMAL_SIZE.height);
    }

    private void loadWindowState() {
        Properties props = loadProperties();

        isMaximized = Boolean.parseBoolean(props.getProperty("main.maximized", "true"));

        if (!isMaximized) {
            int x = Integer.parseInt(props.getProperty("main.x", "0"));
            int y = Integer.parseInt(props.getProperty("main.y", "0"));

            // Проверяем, чтобы окно не выходило за границы экрана
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            x = Math.max(0, Math.min(x, screenSize.width - NORMAL_SIZE.width));
            y = Math.max(0, Math.min(y, screenSize.height - NORMAL_SIZE.height));

            mainFrame.setBounds(x, y, NORMAL_SIZE.width, NORMAL_SIZE.height);
        }
    }

    public void saveWindowState() {
        Properties props = new Properties();

        props.setProperty("main.maximized", String.valueOf(isMaximized));

        if (!isMaximized) {
            props.setProperty("main.x", String.valueOf(mainFrame.getX()));
            props.setProperty("main.y", String.valueOf(mainFrame.getY()));
        }

        saveProperties(props);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_PATH);

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return props;
    }

    private void saveProperties(Properties props) {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            props.store(fos, "Window State Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}