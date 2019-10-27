package com.zylex.betbot.service.bet;


import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.DriverManager;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BetBot {

    public static WebDriver driver;

    public static void main(String[] args) throws IOException, InterruptedException {
        chooseAllEligibleCoefficients();
//        String login = args[0];
//        String password = args[1];
//        DriverManager driverManager = new DriverManager();
//        driverManager.initiateDrivers(1, false);
//        try {
//            driver = driverManager.getDriver();
//            driver.navigate().to("https://1xstavka.ru/line/Football/88637-England-Premier-League/");
//
//            driver.findElement(By.className("base_auth_form")).click();
//            driver.findElements(By.className("c-input-material__input")).get(0).sendKeys(login);
//            driver.findElements(By.className("c-input-material__input")).get(1).sendKeys(password);
//            driver.findElement(By.className("auth-button")).click();
//
//            exitFromAccount();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//            reader.readLine();
//        } finally {
//            driverManager.quitDrivers();
//        }

        // Шаг 2 - выбрать все отфильтрованные матчи
        // Шаг 3 - произвести ставку
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private static void chooseAllEligibleCoefficients() throws IOException {
        File file = new File("results/filtered_results.csv");
        List<String> lines = Files.readAllLines(file.toPath());
        List<Game> eligibleGames = new ArrayList<>();
        for (String line : lines) {
            String[] fields = line.split(";");
            Game game = new Game(fields[0],
                    LocalDateTime.parse(fields[2] + ";" + fields[3], FORMATTER),
                    fields[4],
                    fields[5],
                    fields[6],
                    fields[7],
                    fields[8],
                    fields[9],
                    fields[10],
                    fields[1]);
            eligibleGames.add(game);
        }
        eligibleGames.forEach(System.out::println);
    }

    private static void exitFromAccount() throws InterruptedException {
        Thread.sleep(3000);
        WebElement lkWrap = driver.findElement(By.className("wrap_lk"));
        Actions actions = new Actions(driver);
        actions.moveToElement(lkWrap).build().perform();
        Thread.sleep(1000);
        WebElement exitElement = driver.findElements(By.className("lk_header_options_item")).get(4);
        exitElement.click();
        Thread.sleep(1000);
        driver.findElement(By.className("swal2-confirm")).click();
    }
}
