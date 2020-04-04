package com.zylex.betbot.controller.logger;

import com.zylex.betbot.service.driver.DriverManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log DriverManager.
 */
public class DriverConsoleLogger extends ConsoleLogger {

    private final static Logger LOG = LoggerFactory.getLogger(DriverManager.class);

    /**
     * Log start message.
     */
    public void startLogMessage() {
        String output = "Starting web driver: ...";
        writeInLine("\n" + output);
        LOG.info(output);
    }

    /**
     * Log driver start.
     */
    public void logDriver() {
        String output = "Starting web driver: complete";
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        writeLineSeparator();
        LOG.info(output);
    }
}
