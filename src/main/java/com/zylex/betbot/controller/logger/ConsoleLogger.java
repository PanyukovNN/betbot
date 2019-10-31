package com.zylex.betbot.controller.logger;

import org.apache.commons.lang3.StringUtils;

abstract class ConsoleLogger {

    void writeLineSeparator() {
        writeInLine("\n" + StringUtils.repeat("-", 50));
    }

    void writeErrorMessage(String message) {
        System.err.print(message);
    }

    synchronized void writeInLine(String message) {
        System.out.print(message);
    }
}
