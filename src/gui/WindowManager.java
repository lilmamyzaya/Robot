package gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class WindowManager {
    private final JDesktopPane desktopPane;
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/robots_config.properties";

    public WindowManager(JDesktopPane desktopPane) {
        this.desktopPane = desktopPane;
    }

    public void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    public void saveWindowState(JFrame frame) {
        Properties props = new Properties();
        int state = frame.getExtendedState();
        props.setProperty("state", String.valueOf(state));

        if (state == Frame.NORMAL) {
            props.setProperty("main.x", String.valueOf(frame.getX()));
            props.setProperty("main.y", String.valueOf(frame.getY()));
            props.setProperty("main.width", String.valueOf(frame.getWidth()));
            props.setProperty("main.height", String.valueOf(frame.getHeight()));
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

        if (!configFile.exists()) {
            setDefaultBounds(frame);
            return;
        }

        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            props.load(fis);

            int state = Integer.parseInt(props.getProperty("state", String.valueOf(JFrame.NORMAL)));

            if (state == Frame.NORMAL) {
                int x = Integer.parseInt(props.getProperty("main.x", "100"));
                int y = Integer.parseInt(props.getProperty("main.y", "100"));
                int width = Integer.parseInt(props.getProperty("main.width", "800"));
                int height = Integer.parseInt(props.getProperty("main.height", "600"));
                frame.setBounds(x, y, width, height);
            }

            frame.setExtendedState(state);

            for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
                loadInternalFrameState(internalFrame, internalFrame.getTitle(), props);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            setDefaultBounds(frame);
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

    private void setDefaultBounds(JFrame frame) {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);
    }
}
