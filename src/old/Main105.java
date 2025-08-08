package old;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main105 {
    private static final String RESULT_FILE = "/home/ywang/IdeaProjects/WhatsappCheck/src/resultats-roomz.txt";
    private static final  String targetId_B = "2210c58e-393d-4452-a086-650123181ea9";
    private static final  String targetId_D = "639b2da4-11f7-4226-ad79-8c0dfdc6599f";
    private static final  String targetId_F = "6fa52c65-84b5-4338-a081-dc154666db0b";
    private static final  String targetId_A = "6ad2265f-f596-4b47-8eff-00e3662e0d5e";
    private static final  String targetId_C = "5cbdb346-f49a-48c4-8c7b-b0526a51a4f6";

    // --- NOUVEAU pour Google Docs ---
    private static final String APPLICATION_NAME      = "RoomzMonitor";
    private static final String CREDENTIALS_FILE_PATH = "/home/ywang/IdeaProjects/credentials.json";
    private static final String GOOGLE_DOC_ID         = "1jFh1xfHb7p8D3Yx9BZj_Zb1wRD_t7vJcyHO7T_r5tWY";
    /*
    Command line to open chrome : google-chrome --remote-debugging-port=9222 --user-data-dir="/home/ywang/IdeaProjects/profil-personnel"
     */

    // Variables globales pour stocker les anciennes lignes
    private static String oldtargetIdB = "";
    private static String oldtargetIdD = "";
    private static String oldtargetIdF = "";
    private static String oldtargetIdA = "";
    private static String oldtargetIdC = "";
    private static String oldMCStatus = "";
    private static String oldSPStatus = "";
    private static boolean needTimeForMC = true;
    private static boolean needTimeForSP = true;


    public static void main(String[] args) throws InterruptedException {
        FirefoxBinary binary = new FirefoxBinary();
        binary.addCommandLineOptions("--start-debugger-server", "9222");

        FirefoxOptions options = new FirefoxOptions();
        options.setBinary(binary);
        // -no-remote permet d'ouvrir un profil parallèle sans interférer avec une instance existante
        options.addArguments("-no-remote");
        // si vous souhaitez réutiliser un profil spécifique :
        options.addArguments("-profile", "/home/ywang/IdeaProjects/profil-personnel");

        // 3) Lancez Firefox
        WebDriver driver = new FirefoxDriver(options);

        String roomzUrl = "https://viewer.roomz.io/?roomz-public-id=TI-YlnNDfkeIN8o4ZgWeLA";
        driver.get(roomzUrl);
        String roomzHandle  = driver.getWindowHandle();

        // 3) Ouvrez un second onglet sur Messenger
        String messengerUrl    = "https://www.messenger.com/";
        WebDriver messengerTab = driver.switchTo().newWindow(WindowType.TAB);
        messengerTab.get(messengerUrl);
        String messengerHandle = driver.getWindowHandle();

        driver.switchTo().window(roomzHandle);
        Thread.sleep(10000);  // ajustez selon votre connexion / machine
        //System.out.println("Titre : " + driver.getTitle());

        // Planification de la tâche toutes les 5 minutes
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    DayOfWeek today = LocalDate.now().getDayOfWeek();
                    // a) Vérifier la fenêtre temporelle (02:30 - 05:30)
                    LocalTime now = LocalTime.now();
                    LocalTime start = LocalTime.of(6, 30);
                    LocalTime end = LocalTime.of(19, 30);
                    boolean isInWindow = !now.isBefore(start) && !now.isAfter(end);
                    StringBuffer sbFinal = new StringBuffer();

                    if (isInWindow && today != DayOfWeek.SUNDAY && today != DayOfWeek.SATURDAY) {
                            // 2) Revenir sur l’onglet WhatsApp (au cas où on serait ailleurs)
                            String whatsappHandle = "";
                            for (String handle : driver.getWindowHandles()) {
                                driver.switchTo().window(handle);
                                if (driver.getCurrentUrl().startsWith("https://viewer.roomz.io/")) {
                                    whatsappHandle = handle;
                                    break;
                                }
                            }
                            driver.switchTo().window(whatsappHandle);
                            Thread.sleep(1000);

                            String bureau_b = getClassByTargetId(driver, targetId_B);
                            bureau_b = bureau_b.replace("shape workspace-shape fill-", "");
                            String bureau_d = getClassByTargetId(driver, targetId_D);
                            bureau_d = bureau_d.replace("shape workspace-shape fill-", "");
                            String bureau_f = getClassByTargetId(driver, targetId_F);
                            bureau_f = bureau_f.replace("shape workspace-shape fill-", "");
                            String bureau_a = getClassByTargetId(driver, targetId_A);
                            bureau_a = bureau_a.replace("shape workspace-shape fill-", "");
                            String bureau_c = getClassByTargetId(driver, targetId_C);
                            bureau_c = bureau_c.replace("shape workspace-shape fill-", "");


                            // 4) Construire la nouvelle ligne et mettre à jour le flag line1Identical
                            String [] result = generateNewLines(bureau_b, bureau_d, bureau_f, bureau_a, bureau_c, oldtargetIdB, oldtargetIdD, oldtargetIdF, oldtargetIdA, oldtargetIdC);
                            sbFinal.setLength(0);
                            sbFinal.append(result[0]);

                            if (Boolean.parseBoolean(result[1])) {
                                System.out.println("changemnt du B");
                                oldtargetIdB = bureau_b;
                            }
                            if (Boolean.parseBoolean(result[2])) {
                                System.out.println("changemnt du D");
                                oldtargetIdD = bureau_d;
                            }
                            if (Boolean.parseBoolean(result[3])) {
                                System.out.println("changemnt du F");
                                oldtargetIdF = bureau_f;
                            }
                            if (Boolean.parseBoolean(result[4])) {
                                System.out.println("changemnt du A");
                                oldtargetIdA = bureau_a;
                            }
                            if (Boolean.parseBoolean(result[5])) {
                                System.out.println("changemnt du C");
                                oldtargetIdC = bureau_c;
                            }

                            //writeResultToFile(sbFinal);

                            if (Boolean.parseBoolean(result[1]) || Boolean.parseBoolean(result[2]) || Boolean.parseBoolean(result[3]) || Boolean.parseBoolean(result[4]) || Boolean.parseBoolean(result[5])) {
                                System.out.println("dans if  avec needTimeForMC false");
                                needTimeForMC = false;
                                needTimeForSP = false;
                            }
                    }
                    Thread.sleep(3000);

                    String messengerHandle = "";
                    for (String handle : driver.getWindowHandles()) {
                        driver.switchTo().window(handle);
                        if (driver.getCurrentUrl().startsWith("https://www.messenger.com/")) {
                            messengerHandle = handle;
                            break;
                        }
                    }
                    driver.switchTo().window(messengerHandle);

                    WebDriverWait waitMessenger = new WebDriverWait(driver, Duration.ofSeconds(15));
                    // 1) Trouver et activer le champ de recherche
                    By searchLocator = By.xpath("//input[@type='search']"
                            + " | //input[contains(@placeholder,'Rechercher')]");
                    WebElement searchBox = waitMessenger.until(
                            ExpectedConditions.elementToBeClickable(searchLocator)
                    );
                    searchBox.click();
                    searchBox.clear();
                    searchBox.sendKeys("Poirier");

                    // court délai pour laisser l’IHM montrer la liste
                    Thread.sleep(3000);

                    // 2) Cliquer sur la conversation « Marie-Claude Poirier »
                    By convLocator = By.xpath(
                            "/html/body/div[1]/div/div/div/div/div[2]/div/div/div[2]/div/div/div[1]/div[1]/div/div[1]/ul/li[1]/ul/div[2]/li/a/div[1]/div[2]/div/div/span/span");
                    WebElement conversation = waitMessenger.until(
                            ExpectedConditions.refreshed(
                                    ExpectedConditions.elementToBeClickable(convLocator)
                            )
                    );
                    conversation.click();

                    // 4) Récupérer le statut qui suit immédiatement le nom
                    By statusBy = By.xpath("/html/body/div[1]/div/div/div/div/div[2]/div/div/div[1]/div[1]/div/div[3]/div/div/div[1]/div/div/div/div[2]/div/div/div/div/div/div[1]/div/div/div/div/div[2]/div/div/div[4]/div/span");
                    WebElement statusElem = waitMessenger.until(
                            ExpectedConditions.visibilityOfElementLocated(statusBy)
                    );

                    // 4) Afficher le texte
                    String status = statusElem.getText().trim();
                    Thread.sleep(3000);

                    WebDriverWait waitMessenger2 = new WebDriverWait(driver, Duration.ofSeconds(15));
                    // 1) Trouver et activer le champ de recherche
                    By searchLocator2 = By.xpath("//input[@type='search']"
                            + " | //input[contains(@placeholder,'Rechercher')]");
                    WebElement searchBox2 = waitMessenger.until(
                            ExpectedConditions.elementToBeClickable(searchLocator2)
                    );
                    searchBox2.click();
                    searchBox2.clear();
                    searchBox2.sendKeys("Park");

                    // court délai pour laisser l’IHM montrer la liste
                    Thread.sleep(3000);

                    // 2) Cliquer sur la conversation « Marie-Claude Poirier »
                    By convLocator2 = By.xpath(
                            "/html/body/div[1]/div/div/div/div/div[2]/div/div/div[2]/div/div/div[1]/div[1]/div/div[1]/ul/li[1]/ul/div[2]/li/a/div[1]/div[2]/div/div/span/span");
                    WebElement conversation2 = waitMessenger.until(
                            ExpectedConditions.refreshed(
                                    ExpectedConditions.elementToBeClickable(convLocator2)
                            )
                    );
                    conversation2.click();

                    // 4) Récupérer le statut qui suit immédiatement le nom
                    By statusBy2 = By.xpath("/html/body/div[1]/div/div/div/div/div[2]/div/div/div[1]/div[1]/div/div[3]/div/div/div[1]/div/div/div/div[2]/div/div/div/div/div/div[1]/div/div/div/div/div[2]/div/div/div[4]/div/span");
                    WebElement statusElem2 = waitMessenger.until(
                            ExpectedConditions.visibilityOfElementLocated(statusBy2)
                    );

                    // 4) Afficher le texte
                    String status2 = statusElem2.getText().trim();
                    System.out.println("SP : " + status2);
                    Thread.sleep(3000);

                    if (!oldMCStatus.equals(status) || status.equals("En ligne")) {
                        System.out.println("dans if");
                        oldMCStatus = status;
                        if (needTimeForMC) {
                            sbFinal.setLength(0);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
                            sbFinal.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");
                            sbFinal.append(System.lineSeparator()).append("MC : ").append(status);
                            System.out.println("if Changement uniquement MC - needTimeForMC = true");
                        } else {
                            sbFinal.append("MC : ").append(status);
                            needTimeForMC = true;
                            System.out.println("if Changement les deux MC - needTimeForMC = false");
                        }
                        writeResultsToYanDiscussion(driver, waitMessenger, sbFinal);
                    } else {
                        System.out.println("dand else");
                        if (needTimeForMC) {
                            System.out.println("else if- needTimeForMC = true");
                        } else {
                            needTimeForMC = true;
                            System.out.println("else else - needTimeForMC = false");
                            writeResultsToYanDiscussion(driver, waitMessenger, sbFinal);
                        }
                    }

                    if (!oldSPStatus.equals(status2) || status2.equals("En ligne")) {
                        System.out.println("dans if");
                        oldSPStatus = status2;
                        if (needTimeForSP) {
                            sbFinal.setLength(0);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
                            sbFinal.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");
                            sbFinal.append(System.lineSeparator()).append("SP : ").append(status2);
                            System.out.println("if Changement uniquement SP - needTimeForSP = true");
                        } else {
                            sbFinal.append("SP : ").append(status2);
                            needTimeForSP = true;
                            System.out.println("if Changement les deux SP - needTimeForSP = false");
                        }
                        writeResultsToYanDiscussion(driver, waitMessenger, sbFinal);
                    } else {
                        System.out.println("dand else");
                        if (needTimeForSP) {
                            System.out.println("else if- needTimeForSP = true");
                        } else {
                            needTimeForSP = true;
                            System.out.println("else else - needTimeForSP = false");
                            writeResultsToYanDiscussion(driver, waitMessenger, sbFinal);
                        }
                    }

                    writeResultToFile(sbFinal);
                } catch (Exception e) {
                    System.err.println("Erreur pendant l'exécution de la tâche : " + e.getMessage());
                }
            }
        };

        // Démarrer immédiatement puis toutes les 5 minutes
        scheduler.scheduleAtFixedRate(task, 0, 150, TimeUnit.SECONDS);

        // Empêche le programme de se terminer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            driver.quit();
        }));
    }

    public static void writeResultsToYanDiscussion(WebDriver driver, WebDriverWait waitMessenger, StringBuffer sbFinal) throws InterruptedException {
        By searchLocator2 = By.xpath("//input[@type='search']"
                + " | //input[contains(@placeholder,'Rechercher')]");
        WebElement searchBox2 = waitMessenger.until(
                ExpectedConditions.elementToBeClickable(searchLocator2)
        );
        searchBox2.click();
        searchBox2.clear();
        searchBox2.sendKeys("Wang");

        // court délai pour laisser l’IHM montrer la liste
        Thread.sleep(3000);

        // 2) Cliquer sur la conversation « Marie-Claude Poirier »
        By convLocator2 = By.xpath(
                "/html/body/div[1]/div/div/div/div/div[2]/div/div/div[2]/div/div/div[1]/div[1]/div/div[1]/ul/li[1]/ul/div[2]/li/a/div[1]/div[2]/div/div/span/span");
        WebElement conversation2 = waitMessenger.until(
                ExpectedConditions.refreshed(
                        ExpectedConditions.elementToBeClickable(convLocator2)
                )
        );
        conversation2.click();
        Thread.sleep(2000);
        try {
            // Re-locate the input box fresh each time
            By inputLocator = By.xpath("//div[@role='textbox' and @contenteditable='true']");
            WebElement inputBox = waitMessenger.until(
                    ExpectedConditions.elementToBeClickable(inputLocator)
            );

            Actions actions = new Actions(driver);
            actions.click(inputBox)
                    .sendKeys(sbFinal.toString())
                    .sendKeys(Keys.ENTER)
                    .perform();

            // Short pause between actions
            Thread.sleep(500);

            // Re-locate the input box again
            inputBox = waitMessenger.until(
                    ExpectedConditions.elementToBeClickable(inputLocator)
            );
            inputBox.sendKeys(Keys.ENTER);

        } catch (StaleElementReferenceException e) {
            System.err.println("Stale element encountered, retrying...");
            // Retry the operation
            try {
                By inputLocator = By.xpath("//div[@role='textbox' and @contenteditable='true']");
                WebElement inputBox = waitMessenger.until(
                        ExpectedConditions.elementToBeClickable(inputLocator)
                );
                new Actions(driver)
                        .click(inputBox)
                        .sendKeys(sbFinal.toString())
                        .sendKeys(Keys.ENTER)
                        .sendKeys(Keys.ENTER)
                        .perform();
            } catch (Exception e2) {
                System.err.println("Retry failed: " + e2.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Other error: " + e.getMessage());
        }
    }

    public static String getClassByTargetId (WebDriver driver, String targetId) {
        // 1) Attendre que le SVG soit chargé
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("svg")));

// 2) Construire un locator CSS qui matche l'attribut à la valeur voulue
        By workspaceLocator = By.cssSelector(
                "svg g.shape.workspace-shape[data-workspaceId='" + targetId + "']"
        );

        // 3) Récupérer l'élément
        WebElement workspaceG = wait.until(
                ExpectedConditions.presenceOfElementLocated(workspaceLocator)
        );

        // 4) Lire ses attributs
        String classValue   = workspaceG.getAttribute("class");

        return classValue;
    }

    public static String[] generateNewLines(String bureau_B, String bureau_D, String bureau_F, String bureau_A, String bureau_C, String oldtargetIdB, String oldtargetIdD, String oldtargetIdF,  String oldtargetIdA,  String oldtargetIdC) {
        String[] result = new String[6];

        String changeValueOfB = "false";
        String changeValueOfD = "false";
        String changeValueOfF = "false";
        String changeValueOfA = "false";
        String changeValueOfC = "false";

        StringBuffer sb = new StringBuffer();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
        sb.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");

        if (!bureau_B.equals(oldtargetIdB)) {
            changeValueOfB = "true";
            sb.append(System.lineSeparator()).append(" B : " + bureau_B);
        }
        if (!bureau_D.equals(oldtargetIdD)) {
            changeValueOfD = "true";
            sb.append(System.lineSeparator()).append(" D : " + bureau_D);
        }
        if (!bureau_F.equals(oldtargetIdF)) {
            changeValueOfF = "true";
            sb.append(System.lineSeparator()).append(" F : " + bureau_F);
        }
        if (!bureau_A.equals(oldtargetIdA)) {
            changeValueOfA = "true";
            sb.append(System.lineSeparator()).append(" A : " + bureau_A);
        }
        if (!bureau_C.equals(oldtargetIdC)) {
            changeValueOfC = "true";
            sb.append(System.lineSeparator()).append(" C : " + bureau_C);
        }
        sb.append(System.lineSeparator());

        result[0] =  sb.toString();
        result[1] =  changeValueOfB;
        result[2] =  changeValueOfD;
        result[3] =  changeValueOfF;
        result[4] =  changeValueOfA;
        result[5] =  changeValueOfC;

        return result;
    }

    public static void writeResultToFile(StringBuffer content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_FILE, true))) {
            writer.write(content.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'écriture du fichier : " + e.getMessage(), e);
        }
    }
}