import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Surveillance Telegram (similaire à Main12 mais pour Telegram Web).
 *
 * Hypothèse :
 *  - Tu veux checker le statut "online" / "last seen at XX:YY" pour plusieurs contacts.
 *  - On log le résultat dans le même fichier resultats.txt avec un tag [TG].
 *
 * IMPORTANT :
 *  - Le XPath/structure Telegram peut légèrement varier selon ta version. Il faudra peut-être ajuster
 *    getContactStatus_Telegram() et searchAndClickContact_Telegram() après le premier run.
 */
public class MainTelegram13 {

    private static final String RESULT_FILE = "/home/ywang/IdeaProjects/WhatsappCheck/src/resultats-telegram.txt";

    // anciennes valeurs pour ne pas spammer le fichier si rien n'a changé
    private static String oldStatus_A = "";
    private static String oldStatus_B = "";


    private static String oldLine_A = "";
    private static String oldLine_B = "";

    private static boolean lineIdentical_A = false;
    private static boolean lineIdentical_B = false;

    private static boolean isOutOfWindow = false;
    private static boolean newRestart = true;

    public static void main(String[] args) throws InterruptedException {

        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--user-data-dir=/home/ywang/IdeaProjects/profil-telegram",
                "--remote-debugging-port=9333",
                "--no-sandbox",
                "--disable-dev-shm-usage"
        );

        WebDriver driver = new ChromeDriver(options);

        driver.get("https://web.telegram.org/");
        Thread.sleep(10000); // temps pour que l'UI charge

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    // fenêtre horaire (même logique que Main12)
                    LocalTime now = LocalTime.now();
                    LocalTime start = LocalTime.of(1, 30);
                    LocalTime end = LocalTime.of(5, 30);
                    isOutOfWindow = !now.isBefore(start) && !now.isAfter(end);

                    // logs du tour
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
                    StringBuffer sbHeader = new StringBuffer();
                    sbHeader.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");

                    // Récupération statuts de plusieurs contacts
                    // NOTE: mets ici les noms Telegram EXACTS
                    String contactA = "Domon Frédéric";
                    String contactB = "Alexandra Girel";

                    // Va sur le chat A, lit son statut
                    String statusA = searchAndClickContact_Telegram(driver, contactA, 10);
                    System.out.println("FD : " + statusA);

                    // Construit lignes
                    String newLineA = buildStatusLine(statusA, oldStatus_A, "FD");
                    oldStatus_A = extractLastSeenShort(statusA, oldStatus_A);


                    String statusB =  searchAndClickContact_Telegram(driver, contactB, 10);
                    System.out.println("AG : " + statusB);
                    // Construit lignes
                    String newLineB = buildStatusLine(statusB, oldStatus_B, "AG");
                    oldStatus_B = extractLastSeenShort(statusB, oldStatus_B);


                    // Compare aux anciennes lignes pour éviter le spam
                    if (newLineA.equals(oldLine_A) && !newLineA.contains("online")) {
                        lineIdentical_A = true;
                    } else {
                        lineIdentical_A = false;
                        oldLine_A = newLineA;
                    }
                    // Compare aux anciennes lignes pour éviter le spam
                    if (newLineB.equals(oldLine_B) && !newLineB.contains("online")) {
                        lineIdentical_B = true;
                    } else {
                        lineIdentical_B = false;
                        oldLine_B = newLineB;
                    }


                    // Prépare le bloc à écrire
                    StringBuffer sbOut = new StringBuffer();
                    StringBuffer sbOut2 = new StringBuffer();
                    if (newRestart) {
                        sbHeader.append(" (restart)");
                        sbOut2.append(" (restart) " );
                        newRestart = false;
                    }

                    // Ajoute seulement les lignes qui ont changé
                    if (!lineIdentical_A && !lineIdentical_B) {
                        sbOut.append(oldLine_A).append(" <=> ").append(oldLine_B).append(System.lineSeparator());
                        sbOut2.append(oldLine_A).append(" <=> ").append(oldLine_B).append(System.lineSeparator());
                    }
                    if (!lineIdentical_A && lineIdentical_B) {
                        sbOut.append(oldLine_A).append(" <=> ").append(" ==:== AG").append(System.lineSeparator());
                        sbOut2.append(oldLine_A).append(" <=> ").append(" ==:== AG").append(System.lineSeparator());
                    }
                    if (lineIdentical_A && !lineIdentical_B) {
                        sbOut.append("FD : ==:== ").append(" <=> ").append(oldLine_B).append(System.lineSeparator());
                        sbOut2.append("FD : ==:== ").append(" <=> ").append(oldLine_B).append(System.lineSeparator());
                    }

                    if (sbOut.length() == 0) {
                        // rien de nouveau → juste marquer un ping léger
                        String time = sbHeader.toString();
                        sbHeader.setLength(0);
                        sbHeader.append("_").append(time).append("_").append(System.lineSeparator());
                        writeResultToFile(sbHeader);
                    } else {
                        // on log l'en-tête puis les changements
                        sbHeader.append(System.lineSeparator());
                        sbHeader.append(sbOut);
                        writeResultToFile(sbHeader);
                        searchAndClickSavedMessage(driver, "Saved Messages", 5);
                        sendMessage(driver, sbOut2.toString(), 5);
                    }

                } catch (Exception e) {
                    System.err.println("Erreur Telegram task: " + e.getMessage());
                } finally {
                    // Replanifie comme Main12
                    long nextDelay;
                    if (isOutOfWindow) {
                        if (lineIdentical_A && lineIdentical_B) {
                            nextDelay = 360; // 6 min si rien bouge dans fenêtre nuit
                        } else {
                            nextDelay = 60;  // 1 min si activité
                        }
                    } else {
                        if (lineIdentical_A && lineIdentical_B) {
                            nextDelay = 180; // 3 min journée calme
                        } else {
                            nextDelay = 40;  // 40 sec si bouge
                        }
                    }
                    scheduler.schedule(this, nextDelay, TimeUnit.SECONDS);
                }
            }
        };

        scheduler.schedule(task, 0, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            driver.quit();
        }));
    }

    /**
     * Essaie d'extraire un statut court Telegram.
     * Exemples Telegram :
     *  - "online"
     *  - "last seen at 22:31"
     *  - "last seen recently"
     *
     * On retourne juste "online" ou "22:31" ou "recently".
     */
    private static String extractLastSeenShort(String fullStatus, String oldShort) {
        // if (fullStatus == null) return oldShort;
        String s = fullStatus.toLowerCase().trim();
        if (s.contains("online")) {
            return "online";
        }
        if (s.contains("last seen at")) {
            // ex: "last seen at 22:31"
            return s.replace("last seen at", "").trim();
        }
        if (s.contains("last seen")) {
            // ex: "last seen recently", "last seen within a week"
            return s.replace("last seen", "").trim();
        }
        return s;
    }

    /**
     * Construit une ligne du style "A : online" ou "B : 22:31"
     * Si identique à l'ancien, on remplace par ==:== comme dans Main12.
     */
    private static String buildStatusLine(String currentStatusFull,
                                          String oldShort,
                                          String prefix) {
        String shortNow = extractLastSeenShort(currentStatusFull, oldShort);
        String display;
        if (shortNow.equalsIgnoreCase(oldShort) && !shortNow.equals("online")) {
            if (prefix.equals("AG")) {
                display = " : ==:== " + prefix;
            } else {
                display = prefix + " : ==:== ";
            }
        } else {
            if (prefix.equals("AG")) {
                display = shortNow + " : " + prefix;
            } else {
                display = prefix + " : " + shortNow;
            }
        }
        return display;
    }

    /**
     * Va chercher un contact dans Telegram Web et clique dessus.
     * NOTE: les sélecteurs Telegram Web dépendent un peu du thème.
     * Ici on fait :
     *  - clic dans la barre de recherche
     *  - taper le nom
     *  - cliquer le premier résultat
     *
     * Il est probable que tu doives ajuster les XPaths après un premier run.
     */
    private static String searchAndClickContact_Telegram(WebDriver driver,
                                                       String contactName,
                                                       int timeoutSec)
            throws InterruptedException {
        String result = "";

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));

        // 1) Trouver la barre de recherche Telegram (colonne gauche)
        // Telegram Web moderne a un input[type='text'] avec placeholder genre "Search" ou "Rechercher"
        // On tente d'abord en anglais, puis en français si pas trouvé.
        WebElement searchBar;
        try {
            searchBar = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@type='text' and (contains(@placeholder,'Search') or contains(@placeholder,'Rechercher'))]")
            ));
        } catch (TimeoutException te) {
            throw new RuntimeException("Impossible de trouver la barre de recherche Telegram");
        }

        // 2) Clear
        searchBar.click();
        searchBar.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        searchBar.sendKeys(Keys.DELETE);

        // 3) Tape le nom
        searchBar.sendKeys(contactName);

        // 4) Laisse Telegram remplir la zone résultats
        Thread.sleep(1500);

        /*
         * Structure typique des résultats Telegram Web :
         *
         *  - Chaque résultat "chat" est un container <div> avec des sous-divs genre:
         *      <div class="ListItem ...">
         *          <div class="dialog ...">
         *              ...
         *              <div class="peer-title" ...>NOM DU CONTACT</div>
         *              ...
         *
         * On va donc récupérer tous les ".ListItem" qui ressemblent à une conversation,
         * puis regarder le texte du nom à l'intérieur.
         */

        List<WebElement> chatCandidates = driver.findElements(
                By.xpath("/html/body/div[2]/div/div[1]/div/div[2]/div[2]/div/div[2]/div/div[2]/div/div/div/div[2]/span/span")
        );

        if (chatCandidates.isEmpty()) {
            throw new RuntimeException("Aucun résultat Telegram pour " + contactName);
        }
        result = chatCandidates.get(0).getText();
        Thread.sleep(1000);
        return result;
    }

    private static void searchAndClickSavedMessage(WebDriver driver,
                                                         String contactName,
                                                         int timeoutSec)
            throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));

        // 1) Trouver la barre de recherche Telegram (colonne gauche)
        // Telegram Web moderne a un input[type='text'] avec placeholder genre "Search" ou "Rechercher"
        // On tente d'abord en anglais, puis en français si pas trouvé.
        WebElement searchBar;
        try {
            searchBar = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@type='text' and (contains(@placeholder,'Search') or contains(@placeholder,'Rechercher'))]")
            ));
        } catch (TimeoutException te) {
            throw new RuntimeException("Impossible de trouver la barre de recherche Telegram");
        }

        // 2) Clear
        searchBar.click();
        searchBar.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        searchBar.sendKeys(Keys.DELETE);

        // 3) Tape le nom
        searchBar.sendKeys(contactName);

        // 4) Laisse Telegram remplir la zone résultats
        Thread.sleep(1500);

        int attempts = 0;
        while (attempts < 3) {
            try {
                List<WebElement> results = driver.findElements(By.xpath("/html/body/div[2]/div/div[1]/div/div[2]/div[2]/div/div[2]/div/div[2]/div/div/div"));
                if (results.isEmpty()) {
                    throw new RuntimeException("Aucun résultat trouvé pour " + contactName);
                }
                results.get(0).click();
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }

    private static void writeResultToFile(CharSequence content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_FILE, true))) {
            writer.write(content.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erreur écriture fichier Telegram : " + e.getMessage(), e);
        }
    }

    public static void sendMessage(WebDriver driver, String message, int timeoutSec) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        WebElement inputBox = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[@id=\"editable-message-text\"]")
                )
        );
        inputBox.click();
        inputBox.sendKeys(message);
        inputBox.sendKeys(Keys.ENTER);
    }
}