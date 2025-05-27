package gui;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationManager {
    private static LocalizationManager instance;
    private Locale currentLocale;
    private ResourceBundle messages;

    private LocalizationManager() {
        currentLocale = new Locale("ru"); // По умолчанию русский
        loadLocale(currentLocale);
    }

    public static LocalizationManager getInstance() {
        if (instance == null) {
            instance = new LocalizationManager();
        }
        return instance;
    }

    public void setLocale(Locale locale) {
        currentLocale = locale;
        loadLocale(locale);
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    private void loadLocale(Locale locale) {
        try {
            System.out.println("Attempting to load ResourceBundle for locale: " + locale);
            messages = ResourceBundle.getBundle("messages", locale);
            System.out.println("Successfully loaded ResourceBundle for locale: " + locale);
        } catch (Exception e) {
            System.err.println("Failed to load ResourceBundle for locale: " + locale);
            e.printStackTrace();
            messages = ResourceBundle.getBundle("messages", Locale.ENGLISH); // Резервный вариант
        }
    }

    public String getString(String key) {
        try {
            String value = messages.getString(key);
            System.out.println("Retrieved translation for key '" + key + "': " + value);
            return value;
        } catch (Exception e) {
            System.err.println("Missing translation for key: " + key);
            return key; // Возвращаем ключ как запасной вариант
        }
    }

    public void updateUI(Component component) {
        if (component == null) return;

        if (component instanceof JComponent) {
            JComponent jComponent = (JComponent) component;
            String translationKey = (String) jComponent.getClientProperty("translationKey");
            if (translationKey != null) {
                System.out.println("Updating component " + jComponent.getClass().getSimpleName() + " with translationKey: " + translationKey);
                if (component instanceof JButton) {
                    ((JButton) component).setText(getString(translationKey));
                } else if (component instanceof JLabel) {
                    ((JLabel) component).setText(getString(translationKey));
                } else if (component instanceof JMenu) {
                    ((JMenu) component).setText(getString(translationKey));
                } else if (component instanceof JMenuItem) {
                    ((JMenuItem) component).setText(getString(translationKey));
                } else if (component instanceof JInternalFrame) {
                    ((JInternalFrame) component).setTitle(getString(translationKey));
                }
            } else {
                System.out.println("No translationKey found for component: " + jComponent.getClass().getSimpleName());
            }

            // Рекурсивно обновляем дочерние компоненты
            for (Component child : jComponent.getComponents()) {
                updateUI(child);
            }
        }

        // Обновляем заголовки JFrame
        if (component instanceof JFrame) {
            JFrame frame = (JFrame) component;
            JRootPane rootPane = frame.getRootPane();
            String titleKey = (String) rootPane.getClientProperty("translationKey");
            if (titleKey != null) {
                System.out.println("Updating JFrame title with translationKey: " + titleKey);
                frame.setTitle(getString(titleKey));
            }
            updateUI(frame.getJMenuBar());
            for (Window window : frame.getOwnedWindows()) {
                updateUI(window);
            }
        }
    }
}