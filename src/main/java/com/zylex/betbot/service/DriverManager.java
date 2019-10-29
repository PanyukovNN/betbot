package com.zylex.betbot.service;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.LogType;
import com.zylex.betbot.exception.DriverManagerException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DriverManager {

    private Queue<WebDriver> drivers = new ConcurrentLinkedQueue<>();

    private final int threads;

    public DriverManager(int threads, boolean headless) {
        this.threads = threads;
        initiateDrivers(headless);
    }

    private void initiateDrivers(boolean headless) {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        WebDriverManager.chromedriver().version("77.0.3865.40").setup();
        ConsoleLogger.startLogMessage(LogType.DRIVERS, threads);
        for (int i = 0; i < threads; i++) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--window-size=1980,1020");
            if (headless) {
                options.addArguments("--headless");
            }
            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(600, TimeUnit.SECONDS);
            drivers.add(driver);
            ConsoleLogger.logDriver();
        }
    }

    public int getThreads() {
        return threads;
    }

    /**
     * Getting instance of WebDriver from queue.
     * @return - instance of WebDriver.
     */
    public synchronized WebDriver getDriver() {
        try {
            WebDriver driver = null;
            while (driver == null) {
                driver = drivers.poll();
                Thread.sleep(10);
            }
            return driver;
        } catch (InterruptedException e) {
            throw new DriverManagerException(e.getMessage(), e);
        }
    }

    /**
     * Return driver to queue after usage.
     * @param driver - instance of a WebDriver.
     */
    public synchronized void addDriverToQueue(WebDriver driver) {
        drivers.add(driver);
    }

    /**
     * Quit all drivers, but one.
     */
    public void quitDriversButOne() {
        WebDriver driver = getDriver();
        drivers.forEach(WebDriver::quit);
        drivers.add(driver);
    }

    /**
     * Quit all drivers.
     */
    public void quitDrivers() {
        drivers.forEach(WebDriver::quit);
    }
}
