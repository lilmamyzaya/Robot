package tests;

import log.CircularLogBuffer;
import log.LogEntry;
import log.LogLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CircularLogBufferTest {
    private CircularLogBuffer buffer;
    private static final int CAPACITY = 5;

    @BeforeEach
    void setUp() {
        buffer = new CircularLogBuffer(CAPACITY);
    }

    @Test
    void testAppendAndEviction() {
        // Добавляем 3 записи (меньше ёмкости)
        buffer.append(LogLevel.Info, "Message 1");
        buffer.append(LogLevel.Debug, "Message 2");
        buffer.append(LogLevel.Error, "Message 3");

        assertEquals(3, buffer.size(), "Размер буфера должен быть 3");
        List<LogEntry> entries = new ArrayList<>();
        for (LogEntry entry : buffer.all()) {
            entries.add(entry);
        }
        assertEquals(3, entries.size(), "Должно быть 3 записи");
        assertEquals("Message 1", entries.get(0).getMessage(), "Первая запись должна быть Message 1");
        assertEquals("Message 3", entries.get(2).getMessage(), "Последняя запись должна быть Message 3");

        // Добавляем ещё 3 записи, чтобы превысить ёмкость (итого 6)
        buffer.append(LogLevel.Info, "Message 4");
        buffer.append(LogLevel.Debug, "Message 5");
        buffer.append(LogLevel.Error, "Message 6");

        assertEquals(CAPACITY, buffer.size(), "Размер буфера должен быть равен ёмкости (5)");
        entries.clear();
        for (LogEntry entry : buffer.all()) {
            entries.add(entry);
        }
        assertEquals(CAPACITY, entries.size(), "Должно быть 5 записей после вытеснения");
        assertEquals("Message 2", entries.get(0).getMessage(), "Самая старая запись теперь Message 2");
        assertEquals("Message 6", entries.get(4).getMessage(), "Последняя запись должна быть Message 6");

        // Проверка range
        Iterable<LogEntry> range = buffer.range(1, 3);
        List<LogEntry> rangeEntries = new ArrayList<>();
        for (LogEntry entry : range) {
            rangeEntries.add(entry);
        }
        assertEquals(3, rangeEntries.size(), "Range должен вернуть 3 записи");
        assertEquals("Message 3", rangeEntries.get(0).getMessage(), "Первая запись в range — Message 3");
        assertEquals("Message 5", rangeEntries.get(2).getMessage(), "Последняя запись в range — Message 5");
    }

    @Test
    void testConcurrentAppendAndRead() throws InterruptedException {
        int writerThreads = 4;
        int readerThreads = 4;
        int writesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(writerThreads + readerThreads);
        CountDownLatch latch = new CountDownLatch(writerThreads + readerThreads);

        // Потоки для записи
        for (int i = 0; i < writerThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < writesPerThread; j++) {
                        buffer.append(LogLevel.Info, "Message from thread " + threadId + ": " + j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Потоки для чтения
        for (int i = 0; i < readerThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        Iterable<LogEntry> entries = buffer.range(0, buffer.size());
                        // Просто итерируем, чтобы проверить потокобезопасность
                        for (LogEntry entry : entries) {
                            // Пустое тело, проверяем отсутствие исключений
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Ждём завершения всех потоков
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Все потоки должны завершиться за 5 секунд");

        // Проверяем, что размер буфера не превышает ёмкость
        assertEquals(CAPACITY, buffer.size(), "Размер буфера должен быть равен ёмкости");

        // Проверяем, что записи добавлены
        List<LogEntry> entries = new ArrayList<>();
        for (LogEntry entry : buffer.all()) {
            entries.add(entry);
        }
        assertEquals(CAPACITY, entries.size(), "Должно быть 5 записей");

        executor.shutdown();
    }
}