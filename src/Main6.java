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

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main6 {
    private static final String RESULT_FILE = "/home/ywang/IdeaProjects/WhatsappCheck/src/resultats.txt";

    /*
    Command line to open chrome : google-chrome --remote-debugging-port=9222 --user-data-dir="/home/ywang/IdeaProjects/profil-personnel"
     */

    // Variables globales pour stocker les anciennes lignes
    private static String oldStatusShorten1 = "";
    private static String oldStatusShorten2 = "";
    /*private static String oldStatusShorten3 = "";
    private static String oldStatusShorten4 = "";
    private static String oldStatusShorten5 = "";
    private static String oldStatusShorten6 = "";*/
    private static String oldLine1 = "";
    /*private static String oldLine2 = "";
    private static String oldLine3 = "";*/

    // Flags pour indiquer si les lignes sont identiques
    private static boolean line1Identical = false;
    /*private static boolean line2Identical = false;
    private static boolean line3Identical = false;*/
    private static boolean newRestart = true;


    public static void main(String[] args) throws InterruptedException {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");

        // IMPORTANT : utilisez le même ChromeDriver que votre version de Chrome
        WebDriver driver = new ChromeDriver(options);

        // À présent driver contrôle la fenêtre Chrome existante
        //System.out.println("Titre de la page courante: " + driver.getTitle());

        // Vous pouvez naviguer, cliquer, etc.
        driver.get("https://web.whatsapp.com/");
        Thread.sleep(10000);  // ajustez selon votre connexion / machine
        //System.out.println("Titre : " + driver.getTitle());

        // Planification de la tâche toutes les 5 minutes
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            try {
                // 1) Récupérer l'heure locale actuelle
                LocalTime now = LocalTime.now();

                // 2) Définir les bornes
                LocalTime start = LocalTime.of(2, 30);  // 02:30
                LocalTime end   = LocalTime.of(5, 30);  // 05:30

                // 3) Vérifier si now est après (ou égal) à start ET avant (ou égal) à end
                boolean isInWindow = !now.isBefore(start) && !now.isAfter(end);

                // Exemple d'utilisation
                if (!isInWindow) {
                    /* ***************************** */
                    String newLine1 = "";
                    String newLine2 = "";
                    String newLine3 = "";

                    StringBuffer sb2 = new StringBuffer();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
                    sb2.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");

                    StringBuffer sb = new StringBuffer();
                    //System.out.println("******************************");
                    searchAndClickContact(driver, "Park", 10);
                    String statut2 = getContactStatus(driver, 5);

                    searchAndClickContact(driver, "Domon", 10);
                    String statut = getContactStatus(driver, 5);

                    //searchAndClickContact(driver, "Damien Aguer", 10);
                    //String statut3 = getContactStatus(driver, 5);

                    //searchAndClickContact(driver, "Alexandre Massot", 10);
                    //String statut4 = getContactStatus(driver, 5);

                    String[] sTab = generateNewLine(statut, statut2, oldStatusShorten1, oldStatusShorten2, "FD", "SP");
                    newLine1 = sTab[0];
                    oldStatusShorten1 = sTab[1];
                    oldStatusShorten2 = sTab[2];

                    if (newLine1.equals("FD : ==:== <=> ==:== : SP")) {
                        line1Identical = true;
                    } else {
                        if (oldLine1.equals(newLine1) && !newLine1.contains("en ligne")) {
                            line1Identical = true;
                        } else {
                            line1Identical = false;
                            oldLine1 = newLine1;
                        }
                    }

                    /*sTab = generateNewLine(statut3, statut2, oldStatusShorten3, oldStatusShorten4, "DA", "SP");
                    newLine2 = sTab[0];
                    oldStatusShorten3 = sTab[1];
                    oldStatusShorten4 = sTab[2];

                    if (oldLine2.equals(newLine2)) {
                        line2Identical = true;
                    } else {
                        line2Identical = false;
                        oldLine2 = newLine2;
                    }*/

                    /*sTab = generateNewLine(statut4, statut2, oldStatusShorten5, oldStatusShorten6, "AM", "SP");
                    newLine3 = sTab[0];
                    oldStatusShorten5 = sTab[1];
                    oldStatusShorten6 = sTab[2];

                    if (oldLine3.equals(newLine3)) {
                        line3Identical = true;
                    } else {
                        line3Identical = false;
                        oldLine3 = newLine3;
                    }*/


                    //sb2.append(LocalDateTime.now().format(formatter));
                    if (line1Identical) {
                        //sb2.append("NO CHANGES").append(System.lineSeparator());;
                        String time = sb2.toString();
                        sb2 = new StringBuffer();
                        sb2.append("_").append(time).append("_").append(System.lineSeparator());
                    } else {
                        if (newRestart) {
                            sb2.append(" (restart)");
                            newRestart = false;
                        }
                        if (!line1Identical) {
                            sb2.append(System.lineSeparator()).append(oldLine1).append(System.lineSeparator());
                        }
                       /* if (!line2Identical) {
                            sb2.append(System.lineSeparator()).append(oldLine2).append(System.lineSeparator());
                        }
                        if (!line3Identical) {
                            sb2.append(System.lineSeparator()).append(oldLine3).append(System.lineSeparator());
                        }*/
                        searchAndClickContact(driver, "YAN WANG", 10);
                        sendMessage(driver, sb2.toString(), 5);
                    }
                    writeResultToFile(sb2);

                    /*searchAndClickContact(driver, "YAN WANG", 10);
                    sendMessage(driver, sb2.toString(), 5);*/
                }
            } catch (Exception e) {
                System.err.println("Erreur pendant l'exécution de la tâche : " + e.getMessage());
            }
        };

        // Démarrer immédiatement puis toutes les 5 minutes
        scheduler.scheduleAtFixedRate(task, 0, 3, TimeUnit.MINUTES);

        // Empêche le programme de se terminer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            driver.quit();
        }));
    }

    public static String[] generateNewLine(String statut, String statut2, String oldStatusShorten1, String oldStatusShorten2, String prefix1, String prefix2) {
        String[] result = new String[3];
        StringBuffer sb = new StringBuffer();
        String newLine1 = "";
        //System.out.println("Statut du contact : " + statut);
        if (statut.equals("en ligne")) {
            //System.out.println("Actuellement en ligne");
            String statutShorten = "en ligne";
            sb.append(prefix1 + " : " + statutShorten);
            newLine1 = newLine1 + prefix1 + " : " + statutShorten;
        } else {
            String statutShorten = "";
            if (statut.contains("en ligne aujourd’hui à ")) {
                statutShorten = statut.replace("en ligne aujourd’hui à ", "");
            } else {
                statutShorten = statut.replace("en ligne hier à ", "");
            }
            //System.out.println("Heure : " + statutShorten + " (Domon)");
            if (statutShorten.equals(oldStatusShorten1)) {
                statutShorten = "==:==";
                sb.append(prefix1 + " : " + statutShorten);
            } else {
                oldStatusShorten1 = statutShorten;
                sb.append(prefix1 + " : " + statutShorten);
            }
            newLine1 = newLine1 + prefix1 + " : " + statutShorten;
        }
        sb.append(" <=> ");
        newLine1 = newLine1 + " <=> ";


        //System.out.println("Statut du contact : " + statut2);
        if (statut2.equals("en ligne")) {
            //System.out.println("Actuellement en ligne");
            String statutShorten2 = "en ligne";
            sb.append(statutShorten2 + " : " + prefix2);
            newLine1 = newLine1 + statutShorten2 + " : " + prefix2;
        } else {
            String statutShorten2 = "";
            if (statut2.contains("en ligne aujourd’hui à ")) {
                statutShorten2 = statut2.replace("en ligne aujourd’hui à ", "");
            } else {
                statutShorten2 = statut2.replace("en ligne hier à ", "");
            }
            //System.out.println("Heure : " + statutShorten2 + " (Park)");
            if (statutShorten2.equals(oldStatusShorten2)) {
                statutShorten2 = "==:==";
                sb.append(statutShorten2 + " : " + prefix2);
            } else {
                oldStatusShorten2 = statutShorten2;
                sb.append(statutShorten2 + " : " + prefix2);
            }
            newLine1 = newLine1 + statutShorten2 + " : " + prefix2;
        }
        sb.append(System.lineSeparator());

//        if (newLine1.equals(prefix1 + " : ==:== <=> ==:== : " + prefix2)) {
//            newLine1 = prefix1 + " : " + statut.replace("en ligne aujourd’hui à ", "") + " <=> " + statut2.replace("en ligne aujourd’hui à ", "") + " : " + prefix2;
//        }

        result[0] = newLine1;
        result[1] = oldStatusShorten1;
        result[2] = oldStatusShorten2;
        return result;
    }

    public static void sendMessage(WebDriver driver, String message, int timeoutSec) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        WebElement inputBox = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@contenteditable='true' and @data-tab='10']")
                )
        );
        inputBox.click();
        inputBox.sendKeys(message);
        inputBox.sendKeys(Keys.ENTER);
    }

    public static void writeResultToFile(StringBuffer content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_FILE, true))) {
            writer.write(content.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'écriture du fichier : " + e.getMessage(), e);
        }
    }

    public static String getContactStatus(WebDriver driver, int timeoutSec) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));

        Thread.sleep(6000);
        //By statusLocator = By.xpath("//header//span[@dir='auto' and starts-with(@title,'en ligne')]");
        By statusLocator = By.xpath("//*[@id=\"main\"]/header/div[2]/div[2]/span");
        WebElement statusElem = wait.until(ExpectedConditions.visibilityOfElementLocated(statusLocator));

        return statusElem.getText().trim();
    }

    public static void searchAndClickContact(WebDriver driver, String contactName, int timeoutSec) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));

        // 1) Cliquer sur la barre de recherche
        WebElement searchBar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@contenteditable='true' and @data-tab='3']")));
        searchBar.click();

        // 2) Vider le champ si nécessaire (sélectionner tout et supprimer)
        searchBar.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        searchBar.sendKeys(Keys.DELETE);

        // 3) Taper le nom du contact
        searchBar.sendKeys(contactName);

        // 3) Attendre que les résultats apparaissent (au moins un <span @title>)
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("//span[@title]"), 0));
        Thread.sleep(1500);

        // 4) Cliquer sur le premier résultat avec gestion de StaleElementReferenceException
        int attempts = 0;
        while (attempts < 3) {
            try {
                List<WebElement> results = driver.findElements(By.xpath("//span[@title]"));
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
        throw new RuntimeException("Impossible de cliquer sur le premier résultat après plusieurs tentatives");
    }
}