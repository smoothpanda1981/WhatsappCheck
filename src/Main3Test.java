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
public class Main3Test {
    private static final String RESULT_FILE = "/home/ywang/IdeaProjects/WhatsappCheck/src/resultatsTest.txt";

    /*
    Command line to open chrome : google-chrome --remote-debugging-port=9222 --user-data-dir="/home/ywang/IdeaProjects/profil-personnel"
     */

    // Variables globales pour stocker les anciennes lignes
    private static String oldStatusShorten1 = "";
    private static String oldStatusShorten2 = "";
    private static String oldLine1 = "";

    // Flags pour indiquer si les lignes sont identiques
    private static boolean line1Identical = false;

    public static void main(String[] args) throws InterruptedException {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");


            try {
                    //System.out.println("******************************");
                    String[] fTab = new String[] {"22:42","22:42","22:42","23:09","23:09","23:17","23:20","23:20","23:20","23:20","23:20","23:20","en ligne","23:47","23:47","en ligne", "en ligne hier à 23:59", "en ligne hier à 23:59", "en ligne hier à 23:59"};
                    String[] sTab = new String[] {"en ligne","22:59","23:04","23:04","23:04","23:04","23:04","23:23","23:23","23:23","23:23","23:41","23:41","23:41","23:41","23:41", "en ligne hier à 23:41", "en ligne hier à 23:41", "en ligne hier à 23:41"};

                    for (int i= 0; i< fTab.length; i++) {
                        String newLine1 = "";

                        StringBuffer sb = new StringBuffer();


                        String statut = fTab[i];
                        //System.out.println("Statut du contact : " + statut);
                        if (statut.equals("en ligne")) {
                            //System.out.println("Actuellement en ligne");
                            String statutShorten = "en ligne";
                            sb.append("FD : " + statutShorten);
                            newLine1 = newLine1 + "FD : " + statutShorten;
                        } else {
                            String statutShorten = statut;
                            //System.out.println("Heure : " + statutShorten + " (Domon)");
                            if (statutShorten.equals(oldStatusShorten1)) {
                                statutShorten = "==:==";
                                sb.append("FD : " + statutShorten);
                            } else {
                                oldStatusShorten1 = statutShorten;
                                sb.append("FD : " + statutShorten);
                            }
                            newLine1 = newLine1 + "FD : " + statutShorten;
                        }
                        sb.append(" <=> ");
                        newLine1 = newLine1 + " <=> ";


                        String statut2 = sTab[i];
                        //System.out.println("Statut du contact : " + statut2);
                        if (statut2.equals("en ligne")) {
                            //System.out.println("Actuellement en ligne");
                            String statutShorten2 = "en ligne";
                            sb.append(statutShorten2 + " : SP");
                            newLine1 = newLine1 + statutShorten2 + " : SP";
                        } else {
                            String statutShorten2 = statut2;
                            //System.out.println("Heure : " + statutShorten2 + " (Park)");
                            if (statutShorten2.equals(oldStatusShorten2)) {
                                statutShorten2 = "==:==";
                                sb.append(statutShorten2 + " : SP");
                            } else {
                                oldStatusShorten2 = statutShorten2;
                                sb.append(statutShorten2 + " : SP");
                            }
                            newLine1 = newLine1 + statutShorten2 + " : SP";
                        }
                        sb.append(System.lineSeparator());

                        if (newLine1.equals("FD : ==:== <=> ==:== : SP")) {
                            newLine1 = "FD : " + statut + " <=> " + statut2 + " : SP";
                        }

                        //System.out.println("******************************");

                        if (oldLine1.equals(newLine1)) {
                            line1Identical = true;
                        } else {
                            line1Identical = false;
                            oldLine1 = newLine1;
                        }

                        StringBuffer sb2 = new StringBuffer();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm");
                        sb2.append("*** ").append(LocalDateTime.now().format(formatter)).append(" ***");
                        //sb2.append(LocalDateTime.now().format(formatter));
                        if (line1Identical) {
                           //sb2.append("NO CHANGES").append(System.lineSeparator());;
                            String time = sb2.toString();
                            sb2 = new StringBuffer();
                            sb2.append("_").append(time).append("_").append(System.lineSeparator());
                        } else {
                            if (!line1Identical) {
                                sb2.append(System.lineSeparator()).append(oldLine1).append(System.lineSeparator());
                            }
                        }
                        writeResultToFile(sb2);
                    }
            } catch (Exception e) {
                System.err.println("Erreur pendant l'exécution de la tâche : " + e.getMessage());
            }

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
}