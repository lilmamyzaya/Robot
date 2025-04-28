package gui;

import model.RobotModel;
import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final RobotModel robotModel = new RobotModel();
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/robots_config.properties";
    private final WindowManager windowManager;

    public MainApplicationFrame() {
        // Инициализация
        windowManager = new WindowManager(desktopPane);
        setContentPane(desktopPane);
        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Создаем окна с явным позиционированием
        createWindowsWithFixedPosition();

        // Настройка главного окна
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Открыть на весь экран
        setMinimumSize(new Dimension(950, 850)); // Минимальный размер

        // Обработчик закрытия
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        setVisible(true);
    }

    // Установка размеров и позиций для окон

    private void createWindowsWithFixedPosition() {
        // 1. Окно логов
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setTitle("Протокол работы");
        logWindow.pack();
        Logger.debug("Протокол работает");
        logWindow.setBounds(10, 10, 500, 500);
        logWindow.setVisible(true);
        desktopPane.add(logWindow);

        // 2. Игровое поле
        GameVisualizer visualizer = new GameVisualizer(robotModel);
        GameWindow gameWindow = new GameWindow(visualizer);
        gameWindow.setTitle("Игровое поле");
        gameWindow.setBounds(520, 10, 400, 400);
        gameWindow.setVisible(true);
        desktopPane.add(gameWindow);

        // 3. Окно координат
        RobotCoordinatesWindow coordsWindow = new RobotCoordinatesWindow(robotModel);
        coordsWindow.setTitle("Координаты робота");
        coordsWindow.setBounds(930, 10, 200, 100);
        coordsWindow.setVisible(true);
        desktopPane.add(coordsWindow);

        // Принудительное обновление
        desktopPane.revalidate();
        desktopPane.repaint();
    }

    // Генерация панели меню

    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createFileMenu());
        return menuBar;
    }

    // Меню управления стилем отображения

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

    // Тестовое меню с командами

    private JMenu createTestMenu() {
        JMenu menu = new JMenu("Тесты");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Тестовые команды");

        menu.add(createMenuItem("Сообщение в лог", KeyEvent.VK_L,
                () -> Logger.debug("Новая строка")));

        return menu;
    }

    // Метод для создания пунктов меню

    private JMenuItem createMenuItem(String title, int mnemonic, Runnable action) {
        JMenuItem item = new JMenuItem(title, mnemonic);
        item.addActionListener(event -> action.run());
        return item;
    }

    // Стиль отображения

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            Logger.debug("Ошибка установки темы: " + e.getMessage());
        }
    }

    // Метод выхода

    private JMenu createFileMenu() {
        JMenu menu = new JMenu("Файл");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("Операции с файлом");

        JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_Q);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(event -> exitApplication());

        menu.add(exitItem);
        return menu;
    }

    private void exitApplication() {
        // Настройка текста кнопок для диалога
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");

        int result = JOptionPane.showConfirmDialog(
                this,
                "Вы действительно хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // Сохраняем состояние окон
            windowManager.saveWindowState(this);

            // Завершаем работу модели робота
            robotModel.shutdown();

            // Закрываем все внутренние окна
            for (JInternalFrame frame : desktopPane.getAllFrames()) {
                if (frame instanceof GameWindow) {
                    ((GameWindow) frame).shutdown();
                }
                frame.dispose();
            }

            // Закрываем главное окно
            dispose();

            // Проверка на полное завершение (оригинальная логика)
            EventQueue.invokeLater(() -> {
                for (Window window : Window.getWindows()) {
                    if (window.isDisplayable()) {
                        return;
                    }
                }
                // System.exit(0) удалён по вашему требованию
            });
        }
    }
}