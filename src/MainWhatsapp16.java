import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
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
// https://chromedriver.storage.googleapis.com/index.html?path=114.0.5735.90/
public class MainWhatsapp16 {
    private static final String RESULT_FILE = "/home/ywang/IdeaProjects/WhatsappCheck/src/resultats.txt";
    private static final String RESULT_FILE_WEBEX = "/home/ywang/IdeaProjects/WhatsappCheck/src/resultats-webex.txt";
    private static final String WEBEX_URL = "https://web.webex.com/spaces";
    private static final String RESULT_FILE_ROOMZ = "/home/ywang/IdeaProjects/WhatsappCheck/src/resultats-roomz.txt";
    private static final String ROOMZ_URL = "https://viewer.roomz.io/?roomz-public-id=TI-YlnNDfkeIN8o4ZgWeLA";
    /*
    Command line to open chrome : google-chrome --remote-debugging-port=9222 --user-data-dir="/home/ywang/IdeaProjects/profil-personnel"
    unzip chromedriver_linux64.zip
    chmod +x chromedriver
    sudo mv chromedriver /usr/local/bin/
     */

    // Variables globales pour stocker les anciennes lignes
    private static String oldStatusShorten1_FD = "";
    private static String oldStatusShorten2_SP = "";
    private static String oldStatusShorten5_BR = "";
    private static String oldStatusShorten6_DA = "";
    private static String oldStatusShorten8_AM = "";
    private static String oldStatusShorten11_AG = "";
    private static String oldStatusShorten12_MP = "";
    private static String oldLine1_FD_SP = "";
    private static String oldLine2_AG = "";
    private static String oldLine3_DA = "";
    private static String oldLine4_BR = "";
    private static String oldLine5_AM = "";
    private static String oldLine6_MP = "";

    // Flags pour indiquer si les lignes sont identiques
    private static boolean line1Identical_FD_SP = false;
    private static boolean line2Identical_AG = false;
    private static boolean line3Identical_DA = false;
    private static boolean line4Identical_BR = false;
    private static boolean line5Identical_AM = false;
    private static boolean line6Identical_MP = false;
    private static boolean isOutOfWindow = false;
    private static boolean newRestart = true;

    private static String whatsappHandle = "";
    private static String roomzHandle    = "";
    private static String webexHandle    = "";

    private static String oldLine101_FD = "";
    private static String oldLine102_SP = "";
    private static String oldLine103_AG = "";
    private static String oldLine104_DA = "";
    private static String oldLine105_BR = "";
    private static String oldLine106_AM = "";
    private static String oldLine107_MP = "";
    private static String oldLine108_CP = "";
    private static String oldLine109_CN = "";
    private static boolean line101Identical_FD = false;
    private static boolean line102Identical_SP = false;
    private static boolean line103Identical_AG = false;
    private static boolean line104Identical_DA = false;
    private static boolean line105Identical_BR = false;
    private static boolean line106Identical_AM = false;
    private static boolean line107Identical_MP = false;
    private static boolean line108Identical_CP = false;
    private static boolean line109Identical_CN = false;

    private static String oldSeat1A = "";
    private static String oldSeat1B = "";
    private static String oldSeat1C = "";
    private static String oldSeat1D = "";
    private static String oldSeat1F = "";
    private static String oldSeat2A = "";
    private static String oldSeat2B = "";
    private static String oldSeat2C = "";
    private static String oldSeat2D = "";
    private static String oldSeat2E = "";
    private static String oldSeat2F = "";
    private static boolean lineIdentical_seat1A = false;
    private static boolean lineIdentical_seat1B = false;
    private static boolean lineIdentical_seat1C = false;
    private static boolean lineIdentical_seat1D = false;
    private static boolean lineIdentical_seat1F = false;
    private static boolean lineIdentical_seat2A = false;
    private static boolean lineIdentical_seat2B = false;
    private static boolean lineIdentical_seat2C = false;
    private static boolean lineIdentical_seat2D = false;
    private static boolean lineIdentical_seat2E = false;
    private static boolean lineIdentical_seat2F = false;

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

        // Onglet 1 : WhatsApp
        driver.get("https://web.whatsapp.com/");
        whatsappHandle = driver.getWindowHandle();
        Thread.sleep(10000);  // ajustez selon votre connexion / machine
        //System.out.println("Titre : " + driver.getTitle());

       // Onglet 2 : Roomz (ouvert via JavaScript)
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", ROOMZ_URL);
        Thread.sleep(10000);

        // Onglet 3 : Webex (ouvert via JavaScript)
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", WEBEX_URL);
        Thread.sleep(10000);

        // Planification de la tâche toutes les 5 minutes
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    // a) Vérifier la fenêtre temporelle (02:30 - 05:30)
                    LocalTime now = LocalTime.now();
                    LocalTime start = LocalTime.of(1, 30);
                    LocalTime end = LocalTime.of(5, 30);
                    isOutOfWindow = !now.isBefore(start) && !now.isAfter(end);

                        // 1) Variables préparatoires pour le compte-rendu
                        String newLine1_FD_SP = "";
                        String newLine11_AG = "";
                        String newLine6_DA = "";
                        String newLine5_BR = "";
                        String newLine8_AM = "";
                        String newLine12_MP = "";

                        StringBuffer sb2 = new StringBuffer();
                        StringBuffer sb2W = new StringBuffer();
                        StringBuffer sb2R = new StringBuffer();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
                        sb2.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");
                        sb2R.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");
                        sb2W.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");

                        StringBuffer sb3 = new StringBuffer();

                        // 2) Revenir sur l’onglet WhatsApp (au cas où on serait ailleurs)
                        for (String handle : driver.getWindowHandles()) {
                            driver.switchTo().window(handle);
                            if (driver.getCurrentUrl().startsWith("https://web.whatsapp.com/")) {
                                whatsappHandle = handle;
                                break;
                            }
                        }
                        driver.switchTo().window(whatsappHandle);
                        driver.navigate().refresh();
                        Thread.sleep(5000); // laisser WhatsApp se reconnecter
                        System.out.println("Refreshed WhatsApp");

                       // 3) Récupérer le statut des contacts
                        searchAndClickContact(driver, "Domon", 10);
                        String statut = getContactStatus(driver, 5);

                        searchAndClickContact(driver, "Park", 10);
                        String statut2 = getContactStatus(driver, 5);

                        searchAndClickContact(driver, "Girel", 10);
                        String statut11 = getContactStatus(driver, 5);

                        searchAndClickContact(driver, "Magali", 10);
                        String statut12 = getContactStatus(driver, 5);

                        searchAndClickContact(driver, "Roy", 10);
                        String statut5 = getContactStatus(driver, 5);

                        searchAndClickContact(driver, "Massot", 10);
                        String statut8 = getContactStatus(driver, 5);

                        searchAndClickContact(driver, "Aguer", 10);
                        String statut6 = getContactStatus(driver, 5);

                    // 4) Construire la nouvelle ligne et mettre à jour le flag line1Identical
                        String[] sTab = generateNewLine(statut, statut2, oldStatusShorten1_FD, oldStatusShorten2_SP, "1F", "1S");
                        newLine1_FD_SP = sTab[0];
                        oldStatusShorten1_FD = sTab[1];
                        oldStatusShorten2_SP = sTab[2];

                        String[] sTab11 = generateNewLineSingle(statut11, oldStatusShorten11_AG, "1A");
                        newLine11_AG = sTab11[0];
                        oldStatusShorten11_AG = sTab11[1];

                        String[] sTab6 = generateNewLineSingle(statut6, oldStatusShorten6_DA, "1D");
                        newLine6_DA = sTab6[0];
                        oldStatusShorten6_DA = sTab6[1];

                        String[] sTab5 = generateNewLineSingle(statut5, oldStatusShorten5_BR, "1B");
                        newLine5_BR = sTab5[0];
                        oldStatusShorten5_BR = sTab5[1];

                        String[] sTab8 = generateNewLineSingle(statut8, oldStatusShorten8_AM, "2A");
                        newLine8_AM = sTab8[0];
                        oldStatusShorten8_AM = sTab8[1];

                        String[] sTab12 = generateNewLineSingle(statut12, oldStatusShorten12_MP, "1M");
                        newLine12_MP = sTab12[0];
                        oldStatusShorten12_MP = sTab12[1];

                        if (newLine1_FD_SP.equals("1F : ==:== <=> ==:== : 1S")) {
                            line1Identical_FD_SP = true;
                        } else {
                            if (oldLine1_FD_SP.equals(newLine1_FD_SP) && !newLine1_FD_SP.contains("en ligne")) {
                                line1Identical_FD_SP = true;
                            } else {
                                line1Identical_FD_SP = false;
                                oldLine1_FD_SP = newLine1_FD_SP;
                            }
                        }

                        if (newLine11_AG.equals("1A : ==:==")) {
                            line2Identical_AG = true;
                        } else {
                            if (oldLine2_AG.equals(newLine11_AG) && !newLine11_AG.contains("en ligne")) {
                                line2Identical_AG = true;
                            } else {
                                line2Identical_AG = false;
                                oldLine2_AG = newLine11_AG;
                            }
                        }

                        if (newLine6_DA.equals("1D : ==:==")) {
                            line3Identical_DA = true;
                        } else {
                            if (oldLine3_DA.equals(newLine6_DA) && !newLine6_DA.contains("en ligne")) {
                                line3Identical_DA = true;
                            } else {
                                line3Identical_DA = false;
                                oldLine3_DA = newLine6_DA;
                            }
                        }

                        if (newLine5_BR.equals("1B : ==:==")) {
                            line4Identical_BR = true;
                        } else {
                            if (oldLine4_BR.equals(newLine5_BR) && !newLine5_BR.contains("en ligne")) {
                                line4Identical_BR = true;
                            } else {
                                line4Identical_BR = false;
                                oldLine4_BR = newLine5_BR;
                            }
                        }

                        if (newLine8_AM.equals("2A : ==:==")) {
                            line5Identical_AM = true;
                        } else {
                            if (oldLine5_AM.equals(newLine8_AM) && !newLine8_AM.contains("en ligne")) {
                                line5Identical_AM = true;
                            } else {
                                line5Identical_AM = false;
                                oldLine5_AM = newLine8_AM;
                            }
                        }

                        if (newLine12_MP.equals("1M : ==:==")) {
                            line6Identical_MP = true;
                        } else {
                            if (oldLine6_MP.equals(newLine12_MP) && !newLine12_MP.contains("en ligne")) {
                                line6Identical_MP = true;
                            } else {
                                line6Identical_MP = false;
                                oldLine6_MP = newLine12_MP;
                            }
                        }

                        // 5) Gérer la sortie / envoi si changement détecté
                        if (line1Identical_FD_SP) {
                            String time = sb2.toString();
                            sb2 = new StringBuffer();
                            sb2.append("_").append(time).append("_").append(System.lineSeparator());
                        } else {
                            if (newRestart) {
                                sb2.append(" (restart)");
                                sb3.append("restart : ");
                                newRestart = false;
                            }
                            if (!line1Identical_FD_SP) {
                                    sb2.append(System.lineSeparator()).append(oldLine1_FD_SP).append(System.lineSeparator());
                                    sb3.append(oldLine1_FD_SP).append(System.lineSeparator());
                            }
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3.toString(), 5);
                        }

                        if (!line2Identical_AG) {
                            sb2.append(oldLine2_AG).append(System.lineSeparator());
                            sb3.setLength(0);
                            sb3.append(oldLine2_AG).append(System.lineSeparator());

                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3.toString(), 5);
                        }

                        if (!line6Identical_MP) {
                            sb2.append(oldLine6_MP).append(System.lineSeparator());
                            sb3.setLength(0);
                            sb3.append(oldLine6_MP).append(System.lineSeparator());

                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3.toString(), 5);
                        }

                        if (!line3Identical_DA || !line4Identical_BR || !line5Identical_AM) {
                            sb3.setLength(0);
                            if (!line3Identical_DA) {
                                sb2.append(oldLine3_DA);
                                sb3.append(oldLine3_DA);
                            }
                            if (!line4Identical_BR) {
                                if (!line3Identical_DA) {
                                    sb2.append(" -- ").append(oldLine4_BR);
                                    sb3.append(" -- ").append(oldLine4_BR);
                                } else {
                                    sb2.append(oldLine4_BR);
                                    sb3.append(oldLine4_BR);
                                }
                            }
                            if (!line5Identical_AM) {
                                if (!line3Identical_DA || !line4Identical_BR) {
                                    sb2.append(" -- ").append(oldLine5_AM);
                                    sb3.append(" -- ").append(oldLine5_AM);
                                } else {
                                    sb2.append(oldLine5_AM);
                                    sb3.append(oldLine5_AM);
                                }
                            }
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3.toString(), 5);
                        }
                        writeResultToFile(sb2);
                    Thread.sleep(3000);

                    LocalDateTime now2 = LocalDateTime.now();
                    DayOfWeek day = now2.getDayOfWeek();
                    int hour = now2.getHour();

                    boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                    boolean isWorkingHours = hour >= 7 && hour < 20;

                    if (isWeekday && isWorkingHours) {
                        for (String handle : driver.getWindowHandles()) {
                            driver.switchTo().window(handle);
                            if (driver.getCurrentUrl().startsWith("https://viewer.roomz.io/")) {
                                roomzHandle = handle;
                                break;
                            }
                        }
                        driver.switchTo().window(roomzHandle);
                        driver.navigate().refresh();
                        Thread.sleep(5000); // laisser WhatsApp se reconnecter
                        String seat1A = "6ad2265f-f596-4b47-8eff-00e3662e0d5e";
                        String seat1B = "2210c58e-393d-4452-a086-650123181ea9";
                        String seat1C = "5cbdb346-f49a-48c4-8c7b-b0526a51a4f6";
                        String seat1D = "639b2da4-11f7-4226-ad79-8c0dfdc6599f";
                        String seat1F = "6fa52c65-84b5-4338-a081-dc154666db0b";
                        String seat2A = "97b30ee4-6865-4670-9762-98b1fdb7123f";
                        String seat2B = "4e9f9b32-8df5-45a8-90bd-92038927edbd";
                        String seat2C = "2acfff03-f794-44ba-868c-25a2ec562a47";
                        String seat2D = "2aa8bc92-c66f-4245-9b9a-92a4899fdeb2";
                        String seat2E = "6d59b6fc-d6a4-4ebe-a0ec-c1a1b47d2d69";
                        String seat2F = "49e29c7f-332e-4723-9b68-36cf1468a655";
                        String seat1BColor = searchAndClickRoomz(driver, seat1B, 5);
                        String seat1AColor = searchAndClickRoomz(driver, seat1A, 5);
                        String seat1CColor = searchAndClickRoomz(driver, seat1C, 5);
                        String seat1DColor = searchAndClickRoomz(driver, seat1D, 5);
                        String seat1FColor = searchAndClickRoomz(driver, seat1F, 5);
                        String seat2AColor = searchAndClickRoomz(driver, seat2A, 5);
                        String seat2BColor = searchAndClickRoomz(driver, seat2B, 5);
                        String seat2CColor = searchAndClickRoomz(driver, seat2C, 5);
                        String seat2DColor = searchAndClickRoomz(driver, seat2D, 5);
                        String seat2EColor = searchAndClickRoomz(driver, seat2E, 5);
                        String seat2FColor = searchAndClickRoomz(driver, seat2F, 5);
                        System.out.println("1A : " + seat1AColor);
                        System.out.println("1B : " + seat1BColor);
                        System.out.println("1C : " + seat1CColor);
                        System.out.println("1D : " + seat1DColor);
                        System.out.println("1F : " + seat1FColor);
                        System.out.println("2A : " + seat2AColor);
                        System.out.println("2B : " + seat2BColor);
                        System.out.println("2C : " + seat2CColor);
                        System.out.println("2D : " + seat2DColor);
                        System.out.println("2E : " + seat2EColor);
                        System.out.println("2F : " + seat2FColor);
                        if (oldSeat1A.equals(seat1AColor)) {
                            lineIdentical_seat1A = true;
                        } else {
                            lineIdentical_seat1A = false;
                            oldSeat1A = seat1AColor;
                        }
                        if (oldSeat1B.equals(seat1BColor)) {
                            lineIdentical_seat1B = true;
                        } else {
                            lineIdentical_seat1B = false;
                            oldSeat1B = seat1BColor;
                        }
                        if (oldSeat1C.equals(seat1CColor)) {
                            lineIdentical_seat1C = true;
                        } else {
                            lineIdentical_seat1C = false;
                            oldSeat1C = seat1CColor;
                        }
                        if (oldSeat1D.equals(seat1DColor)) {
                            lineIdentical_seat1D = true;
                        } else {
                            lineIdentical_seat1D = false;
                            oldSeat1D = seat1DColor;
                        }
                        if (oldSeat1F.equals(seat1FColor)) {
                            lineIdentical_seat1F = true;
                        } else {
                            lineIdentical_seat1F = false;
                            oldSeat1F = seat1FColor;
                        }
                        if (oldSeat2A.equals(seat2AColor)) {
                            lineIdentical_seat2A = true;
                        } else {
                            lineIdentical_seat2A = false;
                            oldSeat2A = seat2AColor;
                        }
                        if (oldSeat2B.equals(seat2BColor)) {
                            lineIdentical_seat2B = true;
                        } else {
                            lineIdentical_seat2B = false;
                            oldSeat2B = seat2BColor;
                        }
                        if (oldSeat2C.equals(seat2CColor)) {
                            lineIdentical_seat2C = true;
                        } else {
                            lineIdentical_seat2C = false;
                            oldSeat2C = seat2CColor;
                        }
                        if (oldSeat2D.equals(seat2DColor)) {
                            lineIdentical_seat2D = true;
                        } else {
                            lineIdentical_seat2D = false;
                            oldSeat2D = seat2DColor;
                        }
                        if (oldSeat2E.equals(seat2EColor)) {
                            lineIdentical_seat2E = true;
                        } else {
                            lineIdentical_seat2E = false;
                            oldSeat2E = seat2EColor;
                        }
                        if (oldSeat2F.equals(seat2FColor)) {
                            lineIdentical_seat2F = true;
                        } else {
                            lineIdentical_seat2F = false;
                            oldSeat2F = seat2FColor;
                        }
                        for (String handle : driver.getWindowHandles()) {
                            driver.switchTo().window(handle);
                            if (driver.getCurrentUrl().startsWith("https://web.whatsapp.com/")) {
                                roomzHandle = handle;
                                break;
                            }
                        }
                        driver.switchTo().window(whatsappHandle);
                        StringBuffer sb3R = new StringBuffer();
                        sb3R.append(System.lineSeparator());
                        if (!lineIdentical_seat1A) {
                            sb2R.append("seat A1 : ").append(oldSeat1A).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat A1 : ").append(oldSeat1A).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat1B) {
                            sb2R.append("seat B1 : ").append(oldSeat1B).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat B1 : ").append(oldSeat1B).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat1C) {
                            sb2R.append("seat C1 : ").append(oldSeat1C).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat C1 : ").append(oldSeat1C).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat1D) {
                            sb2R.append("seat D1 : ").append(oldSeat1D).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat D1 : ").append(oldSeat1D).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat1F) {
                            sb2R.append("seat F1 : ").append(oldSeat1F).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat F1 : ").append(oldSeat1F).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat2A) {
                            sb2R.append("seat A2 : ").append(oldSeat2A).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat A2 : ").append(oldSeat2A).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat2B) {
                            sb2R.append("seat B2 : ").append(oldSeat2B).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat B2 : ").append(oldSeat2B).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat2C) {
                            sb2R.append("seat C2 : ").append(oldSeat2C).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat C2 : ").append(oldSeat2C).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat2D) {
                            sb2R.append("seat D2 : ").append(oldSeat2D).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat D2 : ").append(oldSeat2D).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat2E) {
                            sb2R.append("seat E2 : ").append(oldSeat2E).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat E2 : ").append(oldSeat2E).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        if (!lineIdentical_seat2F) {
                            sb2R.append("seat F2 : ").append(oldSeat2F).append(System.lineSeparator());
                            sb3R.setLength(0);
                            sb3R.append("seat F2 : ").append(oldSeat2F).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3R.toString(), 5);
                        }
                        writeResultToFile(sb2R, RESULT_FILE_ROOMZ);
                        Thread.sleep(3000);


                        for (String handle : driver.getWindowHandles()) {
                            driver.switchTo().window(handle);
                            if (driver.getCurrentUrl().startsWith("https://web.webex.com/")) {
                                webexHandle = handle;
                                break;
                            }
                        }
                        driver.switchTo().window(webexHandle);
                        driver.navigate().refresh();
                        Thread.sleep(5000);

                        StringBuffer sb3W = new StringBuffer();
                        sb3W.append(System.lineSeparator());

                        String CN = searchAndClickWebex(driver, "Nlate Camille", 10);
                        System.out.println("2C : " + CN);
                        String MP = searchAndClickWebex(driver, "Preveraud magali", 10);
                        System.out.println("1M : " + MP);
                        String FD = searchAndClickWebex(driver, "Domon Frédéric", 10);
                        System.out.println("1F : " + FD);
                        String CP = searchAndClickWebex(driver, "Perez Cristian", 10);
                        System.out.println("1C : " + CP);
                        String AM = searchAndClickWebex(driver, "Massot Alexandre", 10);
                        System.out.println("2A : " + AM);
                        String AG = searchAndClickWebex(driver, "Girel Alexandra", 10);
                        System.out.println("1A : " + AG);
                        String BR = searchAndClickWebex(driver, "Roy Benjamin", 10);
                        System.out.println("1B : " + BR);
                        String SP = searchAndClickWebex(driver, "Park Stéphanie", 10);
                        System.out.println("1S : " + SP);
                        String DA = searchAndClickWebex(driver, "Aguer Damien", 10);
                        System.out.println("1D : " + DA);

                        if (oldLine101_FD.equals(FD)) {
                            line101Identical_FD = true;
                        } else {
                            if (isMoreThanTenMinutes(FD)) {
                                line101Identical_FD = true;
                            } else {
                                line101Identical_FD = false;
                                oldLine101_FD = FD;
                            }
                        }

                        if (oldLine102_SP.equals(SP)) {
                            line102Identical_SP = true;
                        } else {
                            if (isMoreThanTenMinutes(SP)) {
                                line102Identical_SP = true;
                            } else {
                                line102Identical_SP = false;
                                oldLine102_SP = SP;
                            }
                        }

                        if (oldLine103_AG.equals(AG)) {
                            line103Identical_AG = true;
                        } else {
                            if (isMoreThanTenMinutes(AG)) {
                                line103Identical_AG = true;
                            } else {
                                line103Identical_AG = false;
                                oldLine103_AG = AG;
                            }
                        }

                        if (oldLine104_DA.equals(DA)) {
                            line104Identical_DA = true;
                        } else {
                            if (isMoreThanTenMinutes(DA)) {
                                line104Identical_DA = true;
                            } else {
                                line104Identical_DA = false;
                                oldLine104_DA = DA;
                            }
                        }

                        if (oldLine105_BR.equals(BR)) {
                            line105Identical_BR = true;
                        } else {
                            if (isMoreThanTenMinutes(BR)) {
                                line105Identical_BR = true;
                            } else {
                                line105Identical_BR = false;
                                oldLine105_BR = BR;
                            }
                        }

                        if (oldLine106_AM.equals(AM)) {
                            line106Identical_AM = true;
                        } else {
                            if (isMoreThanTenMinutes(AM)) {
                                line106Identical_AM = true;
                            } else {
                                line106Identical_AM = false;
                                oldLine106_AM = AM;
                            }
                        }

                        if (oldLine107_MP.equals(MP)) {
                            line107Identical_MP = true;
                        } else {
                            if (isMoreThanTenMinutes(MP)) {
                                line107Identical_MP = true;
                            } else {
                                line107Identical_MP = false;
                                oldLine107_MP = MP;
                            }
                        }

                        if (oldLine108_CP.equals(CP)) {
                            line108Identical_CP = true;
                        } else {
                            if (isMoreThanTenMinutes(CP)) {
                                line108Identical_CP = true;
                            } else {
                                line108Identical_CP = false;
                                oldLine108_CP = CP;
                            }
                        }

                        if (oldLine109_CN.equals(CN)) {
                            line109Identical_CN = true;
                        } else {
                            if (isMoreThanTenMinutes(CN)) {
                                line109Identical_CN = true;
                            } else {
                                line109Identical_CN = false;
                                oldLine109_CN = CN;
                            }
                        }

                        for (String handle : driver.getWindowHandles()) {
                            driver.switchTo().window(handle);
                            if (driver.getCurrentUrl().startsWith("https://web.whatsapp.com/")) {
                                roomzHandle = handle;
                                break;
                            }
                        }
                        driver.switchTo().window(whatsappHandle);

                        if (!line101Identical_FD) {
                            sb2W.append("Wx - 1F : ").append(oldLine101_FD).append(System.lineSeparator());
                            sb3W.setLength(0);
                            sb3W.append("Wx - 1F : ").append(oldLine101_FD).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3W.toString(), 5);
                        }
                        if (!line102Identical_SP) {
                            sb2W.append("Wx - 1S : ").append(oldLine102_SP).append(System.lineSeparator());
                            sb3W.setLength(0);
                            sb3W.append("Wx - 1S : ").append(oldLine102_SP).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3W.toString(), 5);
                        }
                        if (!line107Identical_MP) {
                            sb2W.append("Wx - 1M : ").append(oldLine107_MP).append(System.lineSeparator());
                            sb3W.setLength(0);
                            sb3W.append("Wx - 1M : ").append(oldLine107_MP).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3W.toString(), 5);
                        }
                        if (!line103Identical_AG) {
                            sb2W.append("Wx - 1A : ").append(oldLine103_AG).append(System.lineSeparator());
                            sb3W.setLength(0);
                            sb3W.append("Wx - 1A : ").append(oldLine103_AG).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3W.toString(), 5);
                        }
                        if (!line104Identical_DA) {
                            sb2W.append("Wx - 1D : ").append(oldLine104_DA).append(System.lineSeparator());
                            sb3W.setLength(0);
                            sb3W.append("Wx - 1D : ").append(oldLine104_DA).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3W.toString(), 5);
                        }
                        if (!line105Identical_BR) {
                            sb2W.append("Wx - 1B : ").append(oldLine105_BR).append(System.lineSeparator());
                            sb3W.setLength(0);
                            sb3W.append("Wx - 1B : ").append(oldLine105_BR).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3W.toString(), 5);
                        }
                        if (!line106Identical_AM) {
                            sb2W.append("Wx - 2A : ").append(oldLine106_AM).append(System.lineSeparator());
                            sb3W.setLength(0);
                            sb3W.append("Wx - 2A : ").append(oldLine106_AM).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3W.toString(), 5);
                        }
                        if (!line108Identical_CP) {
                            sb2W.append("Wx - 1C : ").append(oldLine108_CP).append(System.lineSeparator());
                            sb3W.setLength(0);
                            sb3W.append("Wx - 1C : ").append(oldLine108_CP).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3W.toString(), 5);
                        }
                        if (!line109Identical_CN) {
                            sb2W.append("Wx - 2C : ").append(oldLine109_CN).append(System.lineSeparator());
                            sb3W.setLength(0);
                            sb3W.append("Wx - 2C : ").append(oldLine109_CN).append(System.lineSeparator());
                            searchAndClickContact(driver, "YAN WANG", 10);
                            sendMessage(driver, sb3W.toString(), 5);
                        }
                        writeResultToFile(sb2W, RESULT_FILE_WEBEX);
                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur pendant l'exécution de la tâche : " + e.getMessage());
                } finally {
                    // 6) À la fin de l'exécution de ce tour, on calcule le délai d'attente avant le prochain
                    long nextDelay;
                    if (isOutOfWindow) {
                        if (line1Identical_FD_SP && line2Identical_AG) {
                            if (!line4Identical_BR || !line5Identical_AM || !line6Identical_MP) {
                                // si identique → 4 minutes
                                nextDelay = 240;
                            } else {
                                // si identique → 6 minutes
                                nextDelay = 360;
                            }
                        } else {
                            // si différent → 0.5 minute
                            nextDelay = 60;
                        }
                    } else {
                        if (line1Identical_FD_SP && line2Identical_AG) {
                            if (!line4Identical_BR || !line5Identical_AM || !line6Identical_MP) {
                                // si identique → 1.5 minutes
                                nextDelay = 30;
                            } else {
                                // si identique → 3 minutes
                                nextDelay = 60;
                            }
                        } else {
                            // si différent → 0.5 minute
                            nextDelay = 10;
                        }
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

    public static boolean isMoreThanTenMinutes(String text) {
        // Supprimer tout ce qui se trouve après " | " (ex: "Active 40 minutes ago | En télétravail 🏠")
        if (text.contains(" | ")) {
            text = text.substring(0, text.indexOf(" | ")).trim();
        }

        boolean isMoreThanTenMinutes = false;
        if (text.endsWith("hour ago") || text.endsWith("hours ago")) {
            isMoreThanTenMinutes = true;
        } else if (text.endsWith("minutes ago")) {
            String minutesStr = text.replace("Active ", "").replace(" minutes ago", "").trim();
            if (Integer.parseInt(minutesStr) > 10) {
                isMoreThanTenMinutes = true;
            } else {
                isMoreThanTenMinutes = false;
            }
        } else {
            isMoreThanTenMinutes = false;
        }

        return isMoreThanTenMinutes;
    }

    public static String removeNonBMPCharacters(String text) {
        StringBuilder sb = new StringBuilder();
        text.codePoints().forEach(cp -> {
            if (cp <= 0xFFFF) {
                sb.appendCodePoint(cp);
            }
        });
        return sb.toString();
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

    public static String[] generateNewLineSingle(String statut11, String oldStatusShorten11, String prefix11) {
        String[] result = new String[3];
        StringBuffer sb = new StringBuffer();
        String newLine11 = "";
        //System.out.println("Statut du contact : " + statut);
        if (statut11.equals("en ligne")) {
            //System.out.println("Actuellement en ligne");
            String statutShorten11 = "en ligne";
            sb.append(prefix11 + " : " + statutShorten11);
            newLine11 = newLine11 + prefix11 + " : " + statutShorten11;
        } else {
            String statutShorten11 = "";
            if (statut11.contains("en ligne aujourd’hui à ")) {
                statutShorten11 = statut11.replace("en ligne aujourd’hui à ", "");
            } else {
                statutShorten11 = statut11.replace("en ligne hier à ", "");
            }
            //System.out.println("Heure : " + statutShorten + " (Domon)");
            if (statutShorten11.equals(oldStatusShorten11)) {
                statutShorten11 = "==:==";
                sb.append(prefix11 + " : " + statutShorten11);
            } else {
                oldStatusShorten11 = statutShorten11;
                sb.append(prefix11 + " : " + statutShorten11);
            }
            newLine11 = newLine11 + prefix11 + " : " + statutShorten11;
        }
        sb.append(System.lineSeparator());

//        if (newLine1.equals(prefix1 + " : ==:== <=> ==:== : " + prefix2)) {
//            newLine1 = prefix1 + " : " + statut.replace("en ligne aujourd’hui à ", "") + " <=> " + statut2.replace("en ligne aujourd’hui à ", "") + " : " + prefix2;
//        }

        result[0] = newLine11;
        result[1] = oldStatusShorten11;
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
        String safeMessage = removeNonBMPCharacters(message);
        inputBox.click();
        inputBox.sendKeys(safeMessage);
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

    public static void writeResultToFile(StringBuffer content, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
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
        By statusLocator = By.xpath("/html/body/div[1]/div/div/div/div/div[3]/div/div[4]/div/header/div[1]/div[2]/div[2]/span");


        if (statusLocator != null) {
            WebElement statusElem = wait.until(ExpectedConditions.visibilityOfElementLocated(statusLocator));
            return statusElem.getText().trim();
        } else {
            return "empty";
        }
    }

    public static String getSeatStatus(WebDriver driver, int timeoutSec, String seatValue) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));

        Thread.sleep(6000);
        WebElement element = driver.findElement(By.cssSelector("[data-workspaceId='" + seatValue + "']"));
        WebElement polygon = element.findElement(By.tagName("polygon"));
        String points = polygon.getAttribute("points");
        return points;
    }

    public static void searchAndClickContact(WebDriver driver, String contactName, int timeoutSec) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));

        // 1) Cliquer sur la barre de recherche
        //WebElement searchBar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@contenteditable='true' and @data-tab='3']")));
        WebElement searchBar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[1]/div/div/div/div/div[3]/div/div[3]/div/div[1]/div[1]/div/div/div/div/div/div[2]/input")));

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

    public static String searchAndClickRoomz(WebDriver driver, String workspaceId, int timeoutSec) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        By locator = By.cssSelector("g[data-workspaceId='" + workspaceId + "']");

        try {
            // Attendre que l'élément soit présent dans le DOM
            WebElement workspaceElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(locator)
            );

            // Récupérer la classe CSS qui contient la couleur
            String cssClass = workspaceElement.getAttribute("class");
            System.out.println("🔍 Classe trouvée : " + cssClass + " pour workspaceId : " + workspaceId);

            // Déterminer la couleur
            String color;
            if (cssClass.contains("fill-green")) {
                color = "Free";
            } else if (cssClass.contains("fill-red")) {
                color = "Occupied";
            } else if (cssClass.contains("fill-orange")) {
                color = "Gone";
            } else {
                color = "Unknown";
            }

            System.out.println("📋 Statut de la salle " + workspaceId + " : " + color);
            return color;

        } catch (TimeoutException e) {
            System.out.println("❌ Timeout : workspace non trouvé après " + timeoutSec + "s : " + workspaceId);
            return "NOT_FOUND";
        } catch (NoSuchElementException e) {
            System.out.println("❌ Workspace non trouvé : " + workspaceId);
            return "NOT_FOUND";
        }
    }

    public static String searchAndClickWebex(WebDriver driver, String contactName, int timeoutSec) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);

        // ============================================================
        // ETAPE 1 : Ouvrir/réutiliser la barre de recherche
        // ============================================================
        By searchButtonLocator = By.xpath("//*[@id=\"layout-wrapper\"]/div[3]/div/div/div[1]/div[2]/div/div/mdc-button");
        By searchFieldLocator = By.cssSelector("mdc-searchfield[data-test='general-search-input']");

        boolean searchAlreadyOpen = !driver.findElements(searchFieldLocator).isEmpty();

        if (!searchAlreadyOpen) {
            // Premier appel : cliquer sur le bouton pour ouvrir la recherche
            wait.until(ExpectedConditions.elementToBeClickable(searchButtonLocator)).click();
            System.out.println("✅ Clic sur le bouton recherche");
            Thread.sleep(1000);
        } else {
            System.out.println("✅ Barre de recherche déjà ouverte");
        }

        // ============================================================
        // ETAPE 2 : Trouver l'input via Shadow DOM
        // ============================================================
        WebElement inputField = null;
        String[] jsQueries = {
                "return document.querySelector('mdc-searchfield[data-test=\"general-search-input\"]')?.shadowRoot?.querySelector('input')",
                "return document.activeElement?.shadowRoot?.querySelector('input')",
                "return document.querySelector('mdc-textfield')?.shadowRoot?.querySelector('input')",
                "return Array.from(document.querySelectorAll('mdc-searchfield')).map(e => e.shadowRoot?.querySelector('input')).find(e => e != null)"
        };

        for (String query : jsQueries) {
            try {
                Object result = js.executeScript(query);
                if (result instanceof WebElement) {
                    inputField = (WebElement) result;
                    System.out.println("✅ Input trouvé via JS : " + query);
                    break;
                }
            } catch (Exception e) {
                System.out.println("❌ JS raté : " + query);
            }
        }

        if (inputField == null) {
            throw new RuntimeException("❌ Impossible de trouver le champ de saisie");
        }

        // ============================================================
        // ETAPE 3 : Effacer et taper le nouveau nom
        // ============================================================
        inputField.click();
        Thread.sleep(300);

        // Vider le champ via JavaScript (plus fiable dans le Shadow DOM)
        js.executeScript("arguments[0].value = '';", inputField);
        js.executeScript(
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));",
                inputField
        );
        Thread.sleep(300);

        // Triple-clic pour sélectionner tout puis supprimer (double sécurité)
        inputField.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        inputField.sendKeys(Keys.DELETE);
        Thread.sleep(300);

        // Taper le nouveau nom
        inputField.sendKeys(contactName);
        System.out.println("✅ Texte saisi : " + contactName);

        // ============================================================
        // ETAPE 4 : Capitaliser le nom
        // ============================================================
        Thread.sleep(2000);
        String[] nameParts = contactName.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        String contactNameCapitalized = sb.toString().trim();
        System.out.println("🔍 Recherche avec : " + contactNameCapitalized);

        // ============================================================
        // ETAPE 5 : Attendre que le résultat apparaisse
        // ============================================================
        By personResultLocator = By.cssSelector(
                "mdc-listitem[aria-label*='" + contactNameCapitalized + "']"
        );

        boolean found = false;
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(personResultLocator));
            found = true;
            System.out.println("✅ Résultat trouvé : " + contactNameCapitalized);
        } catch (Exception e) {
            for (String part : nameParts) {
                if (part.length() < 3) continue;
                String partCap = Character.toUpperCase(part.charAt(0)) + part.substring(1).toLowerCase();
                By partLocator = By.cssSelector("mdc-listitem[aria-label*='" + partCap + "']");
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(partLocator));
                    personResultLocator = partLocator;
                    found = true;
                    System.out.println("✅ Résultat trouvé via partie : " + partCap);
                    break;
                } catch (Exception ex) {
                    System.out.println("❌ Non trouvé : " + partCap);
                }
            }
        }

        if (!found) {
            throw new RuntimeException("❌ Aucun résultat pour : " + contactName);
        }

        Thread.sleep(500);

        // ============================================================
        // ETAPE 6 : Hover sur l'avatar, fermer le pop-up, récupérer le statut
        // ============================================================
        String firstPartCap = Character.toUpperCase(nameParts[0].charAt(0))
                + nameParts[0].substring(1).toLowerCase();

        By avatarLocator = By.cssSelector(
                "mdc-listitem[aria-label*='" + firstPartCap + "'] mdc-avatarbutton"
        );
        By statusLocator = By.cssSelector(
                "mdc-listitem[aria-label*='" + firstPartCap + "'] mdc-text[slot='leading-text-secondary-label']"
        );

        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement avatarButton = wait.until(
                        ExpectedConditions.presenceOfElementLocated(avatarLocator)
                );

                // Hover → ouvre le pop-up
                actions.moveToElement(avatarButton).perform();
                System.out.println("✅ Hover sur l'avatar de : " + contactNameCapitalized);
                Thread.sleep(2000);

                // Déplacer la souris → ferme le pop-up
                actions.moveByOffset(800, 0).perform();
                System.out.println("✅ Souris déplacée, pop-up fermé");
                Thread.sleep(1000);

                // Récupérer le statut
                WebElement statusElement = driver.findElement(statusLocator);
                String statusText = statusElement.getText();
                System.out.println("📋 Statut : " + statusText);

                return statusText;

            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ StaleElement retry " + (attempts + 1) + "/3");
                attempts++;
                Thread.sleep(500);
            }
        }

        throw new RuntimeException("❌ Impossible de récupérer le statut après 3 tentatives");
    }
}