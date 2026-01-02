import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainTelegramRoomz1 {

    // =========================
    // CONFIG
    // =========================

    private static final String RESULT_FILE =
            "/home/ywang/IdeaProjects/WhatsappCheck/src/resultats-telegram.txt";

    // Roomz
    private static final String ROOMZ_URL =
            "https://viewer.roomz.io/?roomz-public-id=TI-YlnNDfkeIN8o4ZgWeLA";

    private static final String TARGET_ID_B =
            "2210c58e-393d-4452-a086-650123181ea9";

    // Telegram
    private static final String TG_URL = "https://web.telegram.org/a/";
    private static final String TG_CONTACT_A = "Domon Frédéric";
    private static final String TG_CONTACT_B = "Alexandra Girel";

    // =========================
    // STATE
    // =========================

    private static WebDriver telegramDriver;
    private static WebDriver roomzDriver;

    private static String oldRoomzB = "";
    private static String oldTgA = "";
    private static String oldTgB = "";

    // =========================
    // MAIN
    // =========================

    public static void main(String[] args) throws InterruptedException {

        // ---------- TELEGRAM ----------
        FirefoxOptions tgOptions = new FirefoxOptions();
        tgOptions.addArguments("-no-remote", "-profile",
                "/home/ywang/IdeaProjects/profil-telegram");

        telegramDriver = new FirefoxDriver(tgOptions);
        telegramDriver.get(TG_URL);
        Thread.sleep(10000);

        // ---------- ROOMZ ----------
        FirefoxOptions roomzOptions = new FirefoxOptions();
        roomzOptions.addArguments("-no-remote", "-profile",
                "/home/ywang/IdeaProjects/profil-personnel");

        roomzDriver = new FirefoxDriver(roomzOptions);
        roomzDriver.get(ROOMZ_URL);
        Thread.sleep(10000);

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(2);

        scheduler.scheduleAtFixedRate(
                MainTelegramRoomz1::taskTelegram,
                0, 60, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(
                MainTelegramRoomz1::taskRoomz,
                0, 120, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            telegramDriver.quit();
            roomzDriver.quit();
        }));
    }

    // =========================
    // TELEGRAM TASK
    // =========================

    private static void taskTelegram() {
        try {
            String statusA = searchAndGetTelegramStatus(telegramDriver, TG_CONTACT_A, 10);
            String statusB = searchAndGetTelegramStatus(telegramDriver, TG_CONTACT_B, 10);

            if (!statusA.equals(oldTgA) || !statusB.equals(oldTgB)) {
                oldTgA = statusA;
                oldTgB = statusB;

                String msg =
                        "TG : " + extractShortStatus(statusA)
                                + " <=> " + extractShortStatus(statusB);

                logAndSend("[TG]", msg);
            }

        } catch (Exception e) {
            System.err.println("Telegram error: " + e.getMessage());
        }
    }

    // =========================
    // ROOMZ TASK
    // =========================

    private static void taskRoomz() {
        try {
            String b =
                    getClassByTargetId(roomzDriver, TARGET_ID_B)
                            .replace("shape workspace-shape fill-", "");

            if (!b.equals(oldRoomzB)) {
                oldRoomzB = b;
                logAndSend("[ROOMZ]", "Bureau B = " + b);
            }

        } catch (Exception e) {
            System.err.println("Roomz error: " + e.getMessage());
        }
    }

    // =========================
    // CORE LOGIC
    // =========================

    private static synchronized void logAndSend(String source, String message) {

        String ts = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String payload =
                source + " " + ts + System.lineSeparator()
                        + message + System.lineSeparator();

        // ---- FILE ----
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(RESULT_FILE, true))) {
            writer.write(payload);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }

        // ---- TELEGRAM SAVED MESSAGES ----
        try {
            openSavedMessages(telegramDriver, 5);
            sendTelegramMessage(telegramDriver, payload, 5);
        } catch (Exception e) {
            System.err.println("Telegram send error: " + e.getMessage());
        }
    }

    // =========================
    // HELPERS
    // =========================

    private static String extractShortStatus(String s) {
        s = s.toLowerCase();
        if (s.contains("online")) return "online";
        if (s.contains("last seen at"))
            return s.replace("last seen at", "").trim();
        if (s.contains("last seen"))
            return s.replace("last seen", "").trim();
        return s;
    }

    private static String searchAndGetTelegramStatus(WebDriver driver,
                                                     String name,
                                                     int timeout)
            throws InterruptedException {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));

        WebElement search = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='text']")));

        search.click();
        search.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        search.sendKeys(Keys.DELETE);
        search.sendKeys(name);

        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(
                By.xpath("//span[contains(@class,'peer-title')]"));

        if (results.isEmpty())
            throw new RuntimeException("Contact introuvable: " + name);

        return results.get(0).getText();
    }

    private static void openSavedMessages(WebDriver driver, int timeout)
            throws InterruptedException {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        WebElement search = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='text']")));

        search.click();
        search.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        search.sendKeys(Keys.DELETE);
        search.sendKeys("Saved Messages");

        Thread.sleep(1500);

        driver.findElements(By.xpath("//div[contains(@class,'ListItem')]"))
                .get(0).click();
    }

    private static void sendTelegramMessage(WebDriver driver,
                                            String message,
                                            int timeout) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='editable-message-text']")));

        input.click();
        input.sendKeys(message);
        input.sendKeys(Keys.ENTER);
    }

    private static String getClassByTargetId(WebDriver driver, String targetId) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("svg")));

        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(
                        "svg g.shape.workspace-shape[data-workspaceId='" + targetId + "']"
                )));

        return el.getAttribute("class");
    }
}