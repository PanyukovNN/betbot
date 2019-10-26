package com.zylex.betbot.service;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.LogType;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
class DriverManager {

    private Queue<WebDriver> drivers = new ConcurrentLinkedQueue<>();

    /**
     * Initiates chrome drivers in the amount of threads number, and puts them into threadsafe queue.
     * @param threads - number of threads.
     */
    public void initiateDrivers(int threads) {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        WebDriverManager.chromedriver().version("77.0.3865.40").setup();
        ConsoleLogger.startLogMessage(LogType.DRIVERS, threads);
        for (int i = 0; i < threads; i++) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--window-size=1980,1020");
            options.addArguments("--headless");
            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(600, TimeUnit.SECONDS);
            drivers.add(driver);
            ConsoleLogger.logDriver();
        }
    }

    /**
     * Getting instance of WebDriver from queue.
     * @return - instance of WebDriver.
     * @throws InterruptedException - appears because of Thread.sleep.
     */
    public synchronized WebDriver getDriver() throws InterruptedException {
        WebDriver driver = null;
        while (driver == null) {
            driver = drivers.poll();
            Thread.sleep(10);
        }
        return driver;
    }

    /**
     * Return driver to queue after usage.
     * @param driver - instance of a WebDriver.
     */
    public synchronized void addDriverToQueue(WebDriver driver) {
        drivers.add(driver);
    }

    /**
     * Quit all drivers.
     */
    public void quitDrivers() {
        drivers.forEach(WebDriver::quit);
    }
}
