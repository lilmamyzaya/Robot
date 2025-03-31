package log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Источник логов с ограниченным размером очереди и корректным управлением слушателями.
 */
public class LogWindowSource {
    private final int m_iQueueLength;
    private final LinkedList<LogEntry> m_messages;
    private final List<LogChangeListener> m_listeners;

    public LogWindowSource(int iQueueLength) {
        this.m_iQueueLength = iQueueLength;
        this.m_messages = new LinkedList<>();
        this.m_listeners = new LinkedList<>();
    }

    public synchronized void registerListener(LogChangeListener listener) {
        if (!m_listeners.contains(listener)) {
            m_listeners.add(listener);
        }
    }

    public synchronized void unregisterListener(LogChangeListener listener) {
        m_listeners.remove(listener);
    }

    public synchronized void append(LogLevel logLevel, String strMessage) {
        if (m_messages.size() >= m_iQueueLength) {
            m_messages.removeFirst(); // Удаляем старейшее сообщение, если достигнут лимит
        }
        m_messages.addLast(new LogEntry(logLevel, strMessage));

        // Уведомление слушателей
        for (LogChangeListener listener : new LinkedList<>(m_listeners)) {
            listener.onLogChanged();
        }
    }

    public synchronized int size() {
        return m_messages.size();
    }

    public synchronized Iterable<LogEntry> range(int startFrom, int count) {
        if (startFrom < 0 || startFrom >= m_messages.size()) {
            return Collections.emptyList();
        }
        int endIndex = Math.min(startFrom + count, m_messages.size());
        return new LinkedList<>(m_messages.subList(startFrom, endIndex));
    }

    public synchronized Iterable<LogEntry> all() {
        return new LinkedList<>(m_messages);
    }
}
