package com.zylex.betbot.service.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Primary
public class ChromeManager extends DriverManager {

    public void initiateDriver(boolean headless) {
        quitDriver();
        WebDriverManager.chromedriver().setup();
        setupLogging();
        ChromeOptions options = new ChromeOptions();
        driver = headless
                ? new ChromeDriver(options.addArguments("--headless"))
                : new ChromeDriver();
        manageDriver();
        wait = new WebDriverWait(driver, waitTimeout, 100);
        logger.logDriver();
    }

    private void setupLogging() {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        logger.startLogMessage();
    }
}
