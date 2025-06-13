import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Request;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main101 {
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
                    if (today == DayOfWeek.SUNDAY || today == DayOfWeek.SATURDAY) {
                        return;
                    }
                    // a) Vérifier la fenêtre temporelle (02:30 - 05:30)
                    LocalTime now = LocalTime.now();
                    LocalTime start = LocalTime.of(6, 30);
                    LocalTime end = LocalTime.of(19, 30);
                    boolean isInWindow = !now.isBefore(start) && !now.isAfter(end);

                    if (isInWindow) {
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
                        StringBuffer sbFinal = new StringBuffer();
                        sbFinal.setLength(0);
                        sbFinal.append(result[0]);

                        if (Boolean.parseBoolean(result[1])) {
                            oldtargetIdB = bureau_b;
                        }
                        if (Boolean.parseBoolean(result[2])) {
                            oldtargetIdD = bureau_d;
                        }
                        if (Boolean.parseBoolean(result[3])) {
                            oldtargetIdF = bureau_f;
                        }
                        if (Boolean.parseBoolean(result[4])) {
                            oldtargetIdA = bureau_a;
                        }
                        if (Boolean.parseBoolean(result[5])) {
                            oldtargetIdC = bureau_c;
                        }

                        writeResultToFile(sbFinal);

                        if (Boolean.parseBoolean(result[1]) || Boolean.parseBoolean(result[2]) || Boolean.parseBoolean(result[3]) || Boolean.parseBoolean(result[4]) || Boolean.parseBoolean(result[5])) {
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
                            By yanWangLink = By.xpath("//a[.//span[text()='Yan Wang']]");
                            WebElement conversation = waitMessenger.until(
                                    ExpectedConditions.elementToBeClickable(yanWangLink)
                            );
                            conversation.click();

                            // 3) Sélectionner la zone de saisie (textarea) et y insérer sbFinal
                            By inputLocator = By.xpath("//div[@role='textbox' and @contenteditable='true']");
                            WebElement inputBox = waitMessenger.until(
                                    ExpectedConditions.elementToBeClickable(inputLocator)
                            );
                            Actions actions = new Actions(driver);
                            actions.click(inputBox)
                                    .sendKeys(sbFinal.toString())
                                    .sendKeys(Keys.ENTER)
                                    .perform();

                            // 4a) Option 1 : Envoyer en appuyant sur ENTER
                            inputBox.sendKeys(Keys.ENTER);
                        }

                    }
                } catch (Exception e) {
                    System.err.println("Erreur pendant l'exécution de la tâche : " + e.getMessage());
                }
            }
        };

        // Démarrer immédiatement puis toutes les 5 minutes
        scheduler.scheduleAtFixedRate(task, 0, 180, TimeUnit.SECONDS);

        // Empêche le programme de se terminer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            driver.quit();
        }));
    }

    /** Initialise et renvoie un client Google Docs authentifié via compte de service. */
    private static Docs getDocsService() throws Exception {
        // Lit la clé JSON du compte de service
        GoogleCredentials creds = GoogleCredentials
                .fromStream(new FileInputStream(CREDENTIALS_FILE_PATH))
                .createScoped(Collections.singleton(DocsScopes.DOCUMENTS));

        return new Docs.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(creds))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Ajoute (append) du texte à la fin du document Google Docs identifié par docId.
     */
    private static void appendToGoogleDoc(String docId, String text) throws Exception {
        Docs docsService = getDocsService();

        // Construire la requête d'insertion : on positionne index = 1 pour le début,
        // ou index = document_length pour la fin. Ici on append à la fin :
        int endIndex = docsService.documents()
                .get(docId)
                .execute()
                .getBody()
                .getContent()
                .size() - 1;

        Request insertRequest = new Request()
                .setInsertText(new InsertTextRequest()
                        .setText(text + "\n")
                        .setLocation(new com.google.api.services.docs.v1.model.Location()
                                .setIndex(endIndex)));

        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest()
                .setRequests(Collections.singletonList(insertRequest));

        docsService.documents()
                .batchUpdate(docId, body)
                .execute();
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
}