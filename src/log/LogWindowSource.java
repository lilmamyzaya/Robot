package log;

// Управляет хранением сообщений и уведомляет подписчиков

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogWindowSource {
    private final CircularLogBuffer buffer; // Публичное поле
    private final CopyOnWriteArrayList<LogChangeListener> listeners;

    public LogWindowSource(int queueCapacity) {
        this.buffer = new CircularLogBuffer(queueCapacity);
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
        buffer.append(logLevel, strMessage);
        notifyListeners();
    }

    private void notifyListeners() {
        for (LogChangeListener listener : listeners) {
            listener.onLogChanged();
        }
    }

    public int size() {
        return buffer.size();
    }

    public Iterable<LogEntry> range(int startFrom, int count) {
        return buffer.range(startFrom, count);
    }

    public Iterable<LogEntry> all() {
        return buffer.all();
    }

    public CircularLogBuffer getBuffer() {
        return buffer;
    }
}