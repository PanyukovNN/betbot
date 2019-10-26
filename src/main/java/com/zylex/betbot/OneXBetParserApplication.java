package com.zylex.betbot;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.Saver;
import com.zylex.betbot.model.Game;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.ParseProcessor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class OneXBetParserApplication {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        int threads = 6;
        setLoggerProperties();
        DriverManager driverManager = new DriverManager(threads);
        try {
            ParseProcessor parseProcessor = new ParseProcessor();
            List<Game> totalGames = parseProcessor.process(driverManager);
            Saver saver = new Saver();
            saver.processSaving(totalGames);
        } finally {
            driverManager.quitDrivers();
            ConsoleLogger.totalSummarizing();
        }
    }

    @SuppressWarnings("unchecked")
    private static void setLoggerProperties() {
        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        loggers.forEach(logger -> logger.setLevel(org.apache.log4j.Level.OFF));
        System.setProperty("webdriver.chrome.silentOutput", "true");
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
    }
}
