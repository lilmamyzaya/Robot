package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.*;

import log.Logger;

/**
 * Главный фрейм приложения с переработанной системой меню.
 */
public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();

    private static final String CONFIG_PATH = System.getProperty("user.home") + "/robots_config.properties";

    public MainApplicationFrame() {

        setContentPane(desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameVisualizer visualizer = new GameVisualizer(); // создаем визуализатор

        GameWindow gameWindow = new GameWindow(visualizer); // передаем визуализатор в окно
        gameWindow.setSize(400, 400); // опционально
        addWindow(gameWindow); // добавляем в главное окно

        RobotCoordinatesWindow coordsWindow = new RobotCoordinatesWindow(visualizer.getModel());
        addWindow(coordsWindow);// добавляем окно координат


        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setContentPane(desktopPane);
        setJMenuBar(generateMenuBar());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        loadWindowState(this); // Загружаем положение окна

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveWindowState(MainApplicationFrame.this); // Сохраняем перед выходом
                exitApplication();
            }
        });

        setVisible(true);
    }

    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
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
            frame.setIcon(icon); // Свернуть/развернуть
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveWindowState(JFrame frame) {
        Properties props = new Properties();
        int state = frame.getExtendedState();
        props.setProperty("state", String.valueOf(state));

        if (state == Frame.NORMAL) { // Сохраняем размеры только если окно не развернуто
            props.setProperty("main.x", String.valueOf(frame.getX()));
            props.setProperty("main.y", String.valueOf(frame.getY()));
            props.setProperty("main.width", String.valueOf(frame.getWidth()));
            props.setProperty("main.height", String.valueOf(frame.getHeight()));
        }
        // Сохраняем окна
        for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
            saveInternalFrameState(internalFrame, internalFrame.getTitle(), props);
        }

        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            props.store(fos, "Window State Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadWindowState(JFrame frame) {
        Properties props = new Properties();
        File configFile = new File(CONFIG_PATH);

        if (!configFile.exists()) {
            setDefaultBounds(frame);
            return;
        }
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            props.load(fis);

            int state = Integer.parseInt(props.getProperty("main.state", String.valueOf(JFrame.NORMAL)));

            if (state == Frame.NORMAL) { // Если не развернуто - применяем размеры
                int x = Integer.parseInt(props.getProperty("main.x", "100"));
                int y = Integer.parseInt(props.getProperty("main.y", "100"));
                int width = Integer.parseInt(props.getProperty("main.width", "800"));
                int height = Integer.parseInt(props.getProperty("main.height", "600"));
                frame.setBounds(x, y, width, height);
            }

            // Важно: Устанавливаем состояние ПОСЛЕ размеров
            frame.setExtendedState(state);

            // Загружаем окна (все добавлены ДО этого)
            for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
                loadInternalFrameState(internalFrame, internalFrame.getTitle(), props);
            }
        }
        catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            setDefaultBounds(frame);
        }
    }
    private void setDefaultBounds(JFrame frame) {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);
    }


    /**
     * Генерация панели меню.
     */
    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createFileMenu());
        return menuBar;
    }

    /**
     * Меню управления стилем отображения.
     */
    private JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("Режим отображения");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription("Управление режимом отображения приложения");

        menu.add(createMenuItem("Системная схема", KeyEvent.VK_S,
                () -> setLookAndFeel(UIManager.getSystemLookAndFeelClassName())));

        menu.add(createMenuItem("Универсальная схема", KeyEvent.VK_U,
                () -> setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())));

        return menu;
    }

    /**
     * Тестовое меню с командами.
     */
    private JMenu createTestMenu() {
        JMenu menu = new JMenu("Тесты");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Тестовые команды");

        menu.add(createMenuItem("Сообщение в лог", KeyEvent.VK_L,
                () -> Logger.debug("Новая строка")));

        return menu;
    }

    /**
     * Метод для создания пунктов меню.
     */
    private JMenuItem createMenuItem(String title, int mnemonic, Runnable action) {
        JMenuItem item = new JMenuItem(title, mnemonic);
        item.addActionListener(event -> action.run());
        return item;
    }

    /**
     * Стиль отображения.
     */
    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.debug("Ошибка установки темы: " + e.getMessage());
        }
    }
    /**
     * Метод выхода.
     */

    private JMenu createFileMenu() {
        JMenu menu = new JMenu("Файл");
        menu.setMnemonic(KeyEvent.VK_F);

        JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_Q);
        exitItem.addActionListener(event -> exitApplication());

        menu.add(exitItem);
        return menu;
    }

    private void exitApplication() {
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.okButtonText", "ОК");

        int result = JOptionPane.showConfirmDialog(
                this, "Вы действительно хотите выйти?", "Подтверждение",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );
        /**
        if (result == JOptionPane.YES_OPTION) {
            saveWindowState(this);
            System.exit(0);
        }*/
        if (result == JOptionPane.YES_OPTION) {
            saveWindowState(this);

            for (JInternalFrame frame : desktopPane.getAllFrames()) {
                if (frame instanceof GameWindow gw) {
                    gw.shutdown();
                }
            }

            this.dispose();

            EventQueue.invokeLater(() -> {
                boolean hasVisibleWindows = false;
                for (Window window : Window.getWindows()) {
                    if (window.isDisplayable()) {
                        hasVisibleWindows = true;
                        break;
                    }
                }
                if (!hasVisibleWindows) {
                    System.out.println("Приложение завершено");
                }
            });
        }
    }

}
