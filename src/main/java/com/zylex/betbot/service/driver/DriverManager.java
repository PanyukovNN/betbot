package com.zylex.betbot.service.driver;

import com.zylex.betbot.controller.logger.DriverConsoleLogger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Managing web drivers.
 */
@SuppressWarnings("WeakerAccess")
@Service
public abstract class DriverManager {

    protected static final int waitTimeout = 5;

    protected static final DriverConsoleLogger logger = new DriverConsoleLogger();

    protected WebDriver driver;

    protected WebDriverWait wait;

    public WebDriver getDriver() {
        return driver;
    }

    @PreDestroy
    private void preDestroy() {
        quitDriver();
    }

    /**
     * Initiate web driver and return it.
     */
    public abstract void initiateDriver();

    protected void manageDriver() {
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().timeouts().pageLoadTimeout(600, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
    }

    /**
     * Quit driver if it was initiated.
     */
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public WebElement waitElement(Function<String, By> byFunction, String elementName) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(byFunction.apply(elementName)));
        return driver.findElement(byFunction.apply(elementName));
    }

    public List<WebElement> waitElements(Function<String, By> byFunction, String elementName) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(byFunction.apply(elementName)));
        return driver.findElements(byFunction.apply(elementName));
    }
}
