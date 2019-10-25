import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class leagueParser {

    public WebDriver driver;

    public WebDriverWait wait;

    public void processLeagueParsing() throws InterruptedException, IOException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1900,1000");
//        options.addArguments("--headless");
        WebDriverManager.chromedriver().version("77.0.3865.40").setup();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 60);
        try {
            driver.navigate().to("https://1xstavka.ru/line/Football/");

            guaranteedClick("ls-filter__name", 1);
            guaranteedClick("chosen-container", 1);
            guaranteedClick("active-result", 2);
            guaranteedClick("ls-filter__btn", 0);

            WebElement sportMenu = driver.findElements(By.className("sport_menu")).get(2);
            WebElement link = sportMenu.findElement(By.className("link"));

            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", link);

            Thread.sleep(2000);
            String pageSource = driver.getPageSource();
            Document document = Jsoup.parse(pageSource);

            Elements elements = document.select("ul.liga_menu > li > a.link");
            System.out.println(elements.size());
            List<String> links = new ArrayList<>();
            for (Element element : elements) {
                String str = element.attr("href");
                if (str.contains("Football")) {
                    links.add(str);
                }
            }
            links.forEach(System.out::println);
            File file = new File("results/leagues.txt");
            FileUtils.touch(file);
            Files.write(file.toPath(), links);
        } finally {
            driver.quit();
        }
    }

    public void guaranteedClick(String className, int arg) {
        while (true) {
            try {
                wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
                Thread.sleep(500);
                List<WebElement> elements = driver.findElements(By.className(className));
                if (elements.size() > 0) {
                    elements.get(arg).click();
                    break;
                }
            } catch (NoSuchElementException | StaleElementReferenceException | ElementClickInterceptedException | InterruptedException e) {
                System.out.println("Can't click " + className + " " + arg + ", trying again...");
            }
        }
    }
}
