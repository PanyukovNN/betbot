package com.zylex.betbot.service;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

class DriverManagerTest {

    @Test
    void initiateDriver_InitiateAndQuitChromeDriver_SuccessfulInitiateAndQuit() {
        DriverManager driverManager = new DriverManager();
        driverManager.initiateDriver(true);
        driverManager.quitDriver();
    }
}