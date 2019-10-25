import controller.ConsoleLogger;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import service.DriverFactory;
import service.ParseProcessor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        int threads = 4;
        DriverFactory driverFactory = new DriverFactory(threads);

//        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
//        loggers.add(LogManager.getRootLogger());
//        loggers.forEach(logger -> logger.setLevel(org.apache.log4j.Level.OFF));
//        System.setProperty("webdriver.chrome.silentOutput", "true");
//        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);

        try {
            ParseProcessor parseProcessor = new ParseProcessor();
            parseProcessor.process(driverFactory);
        } finally {
            driverFactory.quitDrivers();
            ConsoleLogger.totalSummarizing();
        }
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
}
