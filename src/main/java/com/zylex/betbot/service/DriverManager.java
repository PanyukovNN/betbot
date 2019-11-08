package com.zylex.betbot.service;

import com.zylex.betbot.controller.logger.DriverConsoleLogger;
import com.zylex.betbot.controller.logger.LogType;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Managing chrome drivers.
 */
public class DriverManager {

    private DriverConsoleLogger logger = new DriverConsoleLogger();

    private WebDriver driver;

    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Initiate chrome drivers.
     */
    public void initiateDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        logger.startLogMessage(LogType.DRIVER);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 11.12; rv:68.0) Gecko/20100101 Firefox/67.0");
        if (headless) {
            options.addArguments("--headless");
        }
        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().timeouts().pageLoadTimeout(600, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
        logger.logDriver();
    }
}
