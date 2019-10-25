package controller;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConsoleLogger {

    private static AtomicLong programStartTime = new AtomicLong(System.currentTimeMillis());

    private static AtomicInteger totalGames = new AtomicInteger(0);

    private static AtomicInteger processedGames = new AtomicInteger(0);

    private static int threads;

    private static int processedDrivers = 0;

    public static synchronized void startLogMessage(LogType type, Integer arg) {
        if (type == LogType.DRIVERS) {
            threads = arg;
            writeInLine("Starting chrome drivers: 0/" + arg);
        }
    }

    public static synchronized void logDriver() {
        String output = String.format("Starting chrome drivers: %d/%d",
                ++processedDrivers,
                threads);
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        if (processedDrivers == threads) {
            writeLineSeparator();
        }
    }

    public static synchronized void logGame() {
        String output = String.format("Processing games: %d/%d (%s%%)",
                processedGames.incrementAndGet(),
                totalGames.get(),
                new DecimalFormat("#0.0").format(((double) processedGames.get() / (double) totalGames.get()) * 100).replace(",", "."));
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
    }

    public static void totalSummarizing() {
        writeInLine(String.format("Parsing completed in %s", computeTime(programStartTime.get())));
    }

    private static String computeTime(long startTime) {
        long endTime = System.currentTimeMillis();
        long seconds = (endTime - startTime) / 1000;
        long minutes = seconds / 60;
        long houres = 0;
        if (minutes > 60) {
            houres = minutes / 60;
            minutes = minutes % 60;
        }
        return (houres == 0 ? "" : houres + "h. ")
                + minutes + " min. "
                + seconds % 60 + " sec.";
    }

    private static void writeLineSeparator() {
        writeInLine("\n" + StringUtils.repeat("-", 50));
    }

    private static synchronized void writeInLine(String message) {
        System.out.print(message);
    }
}
