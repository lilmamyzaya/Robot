package log;

// Управляет хранением сообщений и уведомляет подписчиков

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogWindowSource {
    private final int queueCapacity;
    private final List<LogEntry> messages;
    private final List<LogChangeListener> listeners;

    public LogWindowSource(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        this.messages = Collections.synchronizedList(new LinkedList<>());
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public void registerListener(LogChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(LogChangeListener listener) {
        listeners.remove(listener);
    }

    public void append(LogLevel logLevel, String strMessage) {
        synchronized (messages) {
            if (messages.size() >= queueCapacity) {
                messages.remove(0);
            }
            messages.add(new LogEntry(logLevel, strMessage));
        }
        notifyListeners();
    }

    private void notifyListeners() {
        for (LogChangeListener listener : listeners) {
            listener.onLogChanged();
        }
    }

    public int size() {
        synchronized (messages) {
            return messages.size();
        }
    }

    public Iterable<LogEntry> range(int startFrom, int count) {
        synchronized (messages) {
            if (startFrom < 0 || startFrom >= messages.size()) {
                return Collections.emptyList();
            }
            int endIndex = Math.min(startFrom + count, messages.size());
            return new LinkedList<>(messages.subList(startFrom, endIndex));
        }
    }

    public Iterable<LogEntry> all() {
        synchronized (messages) {
            return new LinkedList<>(messages);
        }
    }
}