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
    private static String oldStatus_C = "";

    private static String oldLine_A = "";
    private static String oldLine_B = "";
    private static String oldLine_C = "";

    private static boolean lineIdentical_A = false;
    private static boolean lineIdentical_B = false;
    private static boolean lineIdentical_C = false;

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
                    sbHeader.append("*** [TG] ").append(LocalDateTime.now().format(formatter)).append(" ***");

                    // Récupération statuts de plusieurs contacts
                    // NOTE: mets ici les noms Telegram EXACTS
                    String contactA = "Domon Frédéric";

                    // Va sur le chat A, lit son statut
                    searchAndClickContact_Telegram(driver, contactA, 10);
                    String statusA = getContactStatus_Telegram(driver, 5);


                    // Construit lignes
                    String newLineA = buildStatusLine(statusA, oldStatus_A, "A");
                    oldStatus_A = extractLastSeenShort(statusA, oldStatus_A);
                    updateIdenticalFlagsForTelegram(newLineA, "A");


                    // Compare aux anciennes lignes pour éviter le spam
                    if (newLineA.equals(oldLine_A)) {
                        lineIdentical_A = true;
                    } else {
                        lineIdentical_A = false;
                        oldLine_A = newLineA;
                    }


                    // Prépare le bloc à écrire
                    StringBuffer sbOut = new StringBuffer();
                    if (newRestart) {
                        sbHeader.append(" (restart)");
                        newRestart = false;
                    }

                    // Ajoute seulement les lignes qui ont changé
                    if (!lineIdentical_A) {
                        sbOut.append(oldLine_A).append(System.lineSeparator());
                    }
                    if (!lineIdentical_B) {
                        sbOut.append(oldLine_B).append(System.lineSeparator());
                    }
                    if (!lineIdentical_C) {
                        sbOut.append(oldLine_C).append(System.lineSeparator());
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
                    }

                } catch (Exception e) {
                    System.err.println("Erreur Telegram task: " + e.getMessage());
                } finally {
                    // Replanifie comme Main12
                    long nextDelay;
                    if (isOutOfWindow) {
                        if (lineIdentical_A && lineIdentical_B && lineIdentical_C) {
                            nextDelay = 360; // 6 min si rien bouge dans fenêtre nuit
                        } else {
                            nextDelay = 60;  // 1 min si activité
                        }
                    } else {
                        if (lineIdentical_A && lineIdentical_B && lineIdentical_C) {
                            nextDelay = 180; // 3 min journée calme
                        } else {
                            nextDelay = 40;  // 40 sec si bouge
                        }
                    }
                    scheduler.schedule(this, nextDelay, TimeUnit.SECONDS);
                }
            }

            private void updateIdenticalFlagsForTelegram(String line, String prefix) {
                // Ici tu pourrais gérer un ==:== équivalent comme Main12 si tu veux,
                // je laisse simple pour l’instant.
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
        if (fullStatus == null) return oldShort;
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
        if (shortNow.equalsIgnoreCase(oldShort)) {
            display = prefix + " : ==:==";
        } else {
            display = prefix + " : " + shortNow;
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
    private static void searchAndClickContact_Telegram(WebDriver driver,
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
                By.xpath("//div[contains(@class,'ListItem') and .//div[contains(@class,'peer-title')]]")
        );

        if (chatCandidates.isEmpty()) {
            throw new RuntimeException("Aucun résultat Telegram pour " + contactName);
        }

        // 5) Boucle sur les résultats et clique celui qui match vraiment le nom
        WebElement target = null;
        for (WebElement candidate : chatCandidates) {
            try {
                WebElement title = candidate.findElement(
                        By.xpath(".//div[contains(@class,'peer-title')]")
                );
                String titleText = title.getText().trim();
                // On compare sans accents ni casse trop stricte.
                if (normalize(titleText).equalsIgnoreCase(normalize(contactName))) {
                    target = candidate;
                    break;
                }
            } catch (NoSuchElementException ignore) {
                // pas grave, on essaie le suivant
            }
        }

        // fallback : si rien trouvé en match exact, prend le premier résultat
        if (target == null) {
            target = chatCandidates.get(0);
        }

        // 6) Clique le bon chat
        wait.until(ExpectedConditions.elementToBeClickable(target)).click();

        // 7) Donne 1s pour charger la conversation à droite
        Thread.sleep(1000);
    }

    // petite fonction utilitaire pour comparer "Domon Frédéric" == "Domon Frederic"
    private static String normalize(String s) {
        if (s == null) return "";
        // retire les accents et espaces en double pour rendre la comparaison plus tolérante
        String tmp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", ""); // enlève les diacritiques
        tmp = tmp.replaceAll("\\s+", " ").trim();
        return tmp;
    }

    /**
     * Récupère le statut du contact courant dans Telegram :
     * Dans l'entête du chat, sous le nom du contact, il y a "online" / "last seen at..."
     * On va cibler cet élément.
     *
     * Exemple XPath courant (dark mode) :
     *   //header//*[contains(@class,'peer-status')]/span
     *
     * On essaie d'être large mais tu peux raffiner après inspection.
     */
    private static String getContactStatus_Telegram(WebDriver driver, int timeoutSec) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));

        Thread.sleep(2000);

        WebElement statusElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//header//*[contains(@class,'peer-status') or contains(@class,'chat-info')]")
        ));

        return statusElem.getText().trim();
    }

    private static void writeResultToFile(CharSequence content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_FILE, true))) {
            writer.write(content.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erreur écriture fichier Telegram : " + e.getMessage(), e);
        }
    }
}