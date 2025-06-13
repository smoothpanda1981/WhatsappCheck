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
public class Main10 {
    private static final String RESULT_FILE = "/home/ywang/IdeaProjects/WhatsappCheck/src/resultats.txt";

    /*
    Command line to open chrome : google-chrome --remote-debugging-port=9222 --user-data-dir="/home/ywang/IdeaProjects/profil-personnel"
    unzip chromedriver_linux64.zip
    chmod +x chromedriver
    sudo mv chromedriver /usr/local/bin/
     */

    // Variables globales pour stocker les anciennes lignes
    private static String oldStatusShorten1 = "";
    private static String oldStatusShorten2 = "";
    private static String oldStatusShorten5 = "";
    private static String oldStatusShorten6 = "";
    private static String oldLine1 = "";

    // Flags pour indiquer si les lignes sont identiques
    private static boolean line1Identical = false;
    private static boolean newRestart = true;


    public static void main(String[] args) throws InterruptedException {
        // 2) Créez vos ChromeOptions en y passant les arguments
        ChromeOptions options = new ChromeOptions();
        // ouvrez Chrome avec le même user-data-dir (pour conserver votre profil/logins)
        options.addArguments(
                "--user-data-dir=/home/ywang/IdeaProjects/profil-personnel",
                "--remote-debugging-port=9222"
        );
        // (éventuellement) pour éviter les popups / erreurs de sandbox sous Linux :
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");

        //ChromeOptions options = new ChromeOptions();
        //options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");

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
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    // a) Vérifier la fenêtre temporelle (02:30 - 05:30)
                    LocalTime now = LocalTime.now();
                    LocalTime start = LocalTime.of(2, 35);
                    LocalTime end = LocalTime.of(5, 30);
                    boolean isInWindow = !now.isBefore(start) && !now.isAfter(end);

                    if (!isInWindow) {
                        // 1) Variables préparatoires pour le compte-rendu
                        String newLine1 = "";
                        StringBuffer sb2 = new StringBuffer();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
                        sb2.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");

                        StringBuffer sb3 = new StringBuffer();

                        // 2) Revenir sur l’onglet WhatsApp (au cas où on serait ailleurs)
                        String whatsappHandle = "";
                        for (String handle : driver.getWindowHandles()) {
                            driver.switchTo().window(handle);
                            if (driver.getCurrentUrl().startsWith("https://web.whatsapp.com/")) {
                                whatsappHandle = handle;
                                break;
                            }
                        }
                        driver.switchTo().window(whatsappHandle);
                        Thread.sleep(1000);

                        // 3) Récupérer le statut des contacts
                        searchAndClickContact(driver, "Park", 10);
                        String statut2 = getContactStatus(driver, 5);

                        searchAndClickContact(driver, "Domon", 10);
                        String statut = getContactStatus(driver, 5);

                        // 4) Construire la nouvelle ligne et mettre à jour le flag line1Identical
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

                        // 5) Gérer la sortie / envoi si changement détecté
                        if (line1Identical) {
                            String time = sb2.toString();
                            sb2 = new StringBuffer();
                            sb2.append("_").append(time).append("_").append(System.lineSeparator());
                        } else {
                            if (newRestart) {
                                sb2.append(" (restart)");
                                sb3.append("restart : ");
                                newRestart = false;
                            }
                            if (!line1Identical) {
                                if (!oldLine1.contains("FD : ==:==") && !oldLine1.contains("==:== : SP")) {
                                    searchAndClickContact(driver, "Roy", 10);
                                    String statut5 = getContactStatus(driver, 5);
                                    String br = generateForExtra(statut5, "BR");
                                    if (!br.equals(oldStatusShorten5)) {
                                        oldStatusShorten5 = br;
                                        sb2.append(System.lineSeparator()).append(oldLine1 + " (" + br + ")").append(System.lineSeparator());
                                        sb3.append(oldLine1 + " (" + br + ")").append(System.lineSeparator());
                                    } else {
                                        searchAndClickContact(driver, "Aguer", 10);
                                        String statut6 = getContactStatus(driver, 5);
                                        String da = generateForExtra(statut6, "DA");
                                        if (!da.equals(oldStatusShorten6)) {
                                            oldStatusShorten6 = da;
                                            sb2.append(System.lineSeparator()).append(oldLine1 + " (" + da + ")").append(System.lineSeparator());
                                            sb3.append(oldLine1 + " (" + da + ")").append(System.lineSeparator());
                                        } else {
                                            sb2.append(System.lineSeparator()).append(oldLine1).append(System.lineSeparator());
                                            sb3.append(oldLine1).append(System.lineSeparator());
                                        }
                                    }
                                } else if (oldLine1.contains("FD : ==:==") && !oldLine1.contains("==:== : SP")) {
                                    searchAndClickContact(driver, "Aguer", 10);
                                    String statut6 = getContactStatus(driver, 5);
                                    String da = generateForExtra(statut6, "DA");
                                    if (!da.equals(oldStatusShorten6)) {
                                        oldStatusShorten6 = da;
                                        sb2.append(System.lineSeparator()).append(oldLine1 + " (" + da + ")").append(System.lineSeparator());
                                        sb3.append(oldLine1 + " (" + da + ")").append(System.lineSeparator());
                                    } else {
                                        sb2.append(System.lineSeparator()).append(oldLine1).append(System.lineSeparator());
                                        sb3.append(oldLine1).append(System.lineSeparator());
                                    }
                                } else {
                                    sb2.append(System.lineSeparator()).append(oldLine1).append(System.lineSeparator());
                                    sb3.append(oldLine1).append(System.lineSeparator());
                                }
                            }
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3.toString(), 5);
                        }
                        writeResultToFile(sb2);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur pendant l'exécution de la tâche : " + e.getMessage());
                } finally {
                    // 6) À la fin de l'exécution de ce tour, on calcule le délai d'attente avant le prochain
                    long nextDelay;
                    if (line1Identical) {
                        // si identique → 3 minutes
                        nextDelay = 180;
                    } else {
                        // si différent → 0.5 minute
                        nextDelay = 40;
                    }
                    // On reprogramme la même tâche avec le délai adapté
                    scheduler.schedule(this, nextDelay, TimeUnit.SECONDS);
                }
            }
        };

        // Démarrer immédiatement puis toutes les 5 minutes
        scheduler.schedule(task, 0, TimeUnit.MINUTES);

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

    public static String generateForExtra (String statut5, String prefix5) {
        String result = "";
        if (statut5.equals("en ligne")) {
            //System.out.println("Actuellement en ligne");
            result = prefix5 + " : "  + "en ligne";
        } else {
            if (statut5.contains("en ligne aujourd’hui à ")) {
                result = prefix5 + " : " + statut5.replace("en ligne aujourd’hui à ", "");
            } else {
                result = prefix5 + " : " + statut5.replace("en ligne hier à ", "");
            }
        }
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