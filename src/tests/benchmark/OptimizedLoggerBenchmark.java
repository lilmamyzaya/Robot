package benchmark;

import java.util.LinkedHashMap;
import java.util.Map;

public class OptimizedLoggerBenchmark {
    private static final Map<String, Runnable> TESTS = new LinkedHashMap<>();
    private static final int WARMUP = 10;
    private static final int ITERATIONS = 10;
    private static final int OPERATIONS = 100_000;

    static {
        // Тест прямого вывода в консоль с printf
        TESTS.put("Console", () -> {
            for (int i = 0; i < OPERATIONS; i++) {
                System.out.printf("Test message %d%n", i);
            }
        });

        // Тест с StringBuilder и периодическим сбросом буфера
        TESTS.put("StringBuilder", () -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < OPERATIONS; i++) {
                sb.append("Test message ").append(i).append("\n");
                if (sb.length() > 1_000_000) { // Сброс буфера для больших объемов
                    System.out.print(sb.toString());
                    sb.setLength(0);
                }
            }
            if (sb.length() > 0) {
                System.out.print(sb.toString());
            }
        });

        // Заглушка для логирования в файл (из SimpleLoggerBenchmark)
        TESTS.put("File", () -> {
            for (int i = 0; i < OPERATIONS; i++) {
                logToFile("Test message " + i); // Можно заменить на реальную реализацию
            }
        });
    }

    // Метод логирования в файл (заглушка, можно реализовать реальную запись)
    private static void logToFile(String message) {
    }

    public static void main(String[] args) {
        System.out.printf("Benchmark config: %d warmups, %d iterations, %,d ops/iter%n%n",
                WARMUP, ITERATIONS, OPERATIONS);

        for (Map.Entry<String, Runnable> test : TESTS.entrySet()) {
            benchmark(test.getKey(), test.getValue());
            System.out.println();
        }
    }

    private static void benchmark(String name, Runnable test) {
        // Разогрев JVM
        System.out.printf("Warming up %s...%n", name);
        for (int i = 0; i < WARMUP; i++) {
            test.run();
        }

        // Измерения
        System.out.printf("Measuring %s...%n", name);
        long[] times = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            test.run();
            times[i] = System.nanoTime() - start;
            System.out.printf("Iteration %d: %,d ns%n", i + 1, times[i]);
        }

        // Статистика
        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        for (long time : times) {
            sum += time;
            min = Math.min(min, time);
            max = Math.max(max, time);
        }

        double avg = (double) sum / ITERATIONS;
        double avgOp = avg / OPERATIONS;

        // Вывод результатов
        System.out.printf("%s results:%n", name);
        System.out.printf("Average: %,.2f ns | %,.2f ns/op%n", avg, avgOp);
        System.out.printf("Min:     %,d ns | %,.2f ns/op%n", min, (double) min / OPERATIONS);
        System.out.printf("Max:     %,d ns | %,.2f ns/op%n", max, (double) max / OPERATIONS);
    }
}