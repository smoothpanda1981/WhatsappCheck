import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main108 {
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
    private static boolean needTimeForMC = true;

    // --- NOTION ---
    private static final String NOTION_TOKEN   = "ntn_HiR3304196671dZmPZPJR9a2BDvsUfpFVl1cSQdEDFm0Ew"; // recommandé (sécurité)
    private static final String NOTION_PAGE_ID = "273001f82e2e80acb4ead5b5761d8cb1"; // ta page
    private static final String NOTION_VERSION = "2022-06-28"; // version stable API

    

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
                    LocalTime start = LocalTime.of(6, 50);
                    LocalTime end = LocalTime.of(19, 30);
                    boolean isInWindow = !now.isBefore(start) && !now.isAfter(end);
                    StringBuffer sbFinal = new StringBuffer();

                    //if (isInWindow && today != DayOfWeek.SUNDAY && today != DayOfWeek.SATURDAY) {
                    if (true) {
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
                            Thread.sleep(3000);

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
                            boolean hasChanges =
                                Boolean.parseBoolean(result[1]) ||
                                        Boolean.parseBoolean(result[2]) ||
                                        Boolean.parseBoolean(result[3]) ||
                                        Boolean.parseBoolean(result[4]) ||
                                        Boolean.parseBoolean(result[5]);
                            sbFinal.setLength(0);
                            if (hasChanges) {
                                sbFinal.append(result[0]); // on n’alimente que s’il y a un vrai diff
                            }
                            // System.out.println("Res : " + sbFinal.toString());

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
                    }
                    Thread.sleep(3000);
                    // On n'écrit que si on a détecté un changement concret
                    if (sbFinal.length() > 0) {
                        writeResultToFile(sbFinal);
                        writeResultToNotion(sbFinal.toString());
                    }
                } catch (Exception e) {
                    System.err.println("Erreur pendant l'exécution de la tâche : " + e.getMessage());
                }
            }
        };

        // Démarrer immédiatement puis toutes les 5 minutes
        scheduler.scheduleAtFixedRate(task, 0, 120, TimeUnit.SECONDS);

        // Empêche le programme de se terminer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            driver.quit();
        }));
    }

    // Écrit dans Notion en ajoutant des blocs "code" à la page.
    public static void writeResultToNotion(String content) {
        if (NOTION_TOKEN == null || NOTION_TOKEN.isBlank()) {
            System.err.println("NOTION_TOKEN manquant. Définis la variable d'environnement NOTION_TOKEN.");
            return;
        }
        if (NOTION_PAGE_ID == null || NOTION_PAGE_ID.isBlank()) {
            System.err.println("NOTION_PAGE_ID manquant. Définis la variable d'environnement NOTION_PAGE_ID.");
            return;
        }

        // Découpe le contenu en blocs raisonnables pour Notion
        String[] chunks = splitForNotion(content, 1800); // ~1800 caractères par bloc (sécurité)

        String jsonBody = buildChildrenPayload(chunks);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.notion.com/v1/blocks/" + NOTION_PAGE_ID + "/children"))
                .header("Authorization", "Bearer " + NOTION_TOKEN)
                .header("Notion-Version", NOTION_VERSION)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        int maxAttempts = 3;
        long backoffMs = 800;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                int sc = resp.statusCode();
                if (sc >= 200 && sc < 300) {
                    System.out.println("Notion: écriture OK (" + chunks.length + " bloc(s)).");
                    return;
                }
                System.err.println("Notion: échec [" + sc + "] tentative " + attempt + "/" + maxAttempts + " - " + resp.body());
            } catch (Exception e) {
                System.err.println("Notion: exception tentative " + attempt + "/" + maxAttempts + " - " + e.getMessage());
            }

            try { Thread.sleep(backoffMs); } catch (InterruptedException ignored) {}
            backoffMs *= 2; // simple backoff
        }

        System.err.println("Notion: abandon après " + maxAttempts + " tentatives.");
    }

    // Construit un payload avec 1..N blocs "code" (garde la mise en forme multi-lignes)
    private static String buildChildrenPayload(String[] chunks) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"children\":[");
        for (int i = 0; i < chunks.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("""
            {
              "object": "block",
              "type": "code",
              "code": {
                "language": "plain text",
                "rich_text": [
                  { "type": "text", "text": { "content": %s } }
                ]
              }
            }
            """.formatted(toJsonString(chunks[i])));
        }
        sb.append("]}");
        return sb.toString();
    }

    // Découpe en morceaux de longueur max, en essayant de couper aux retours ligne
    private static String[] splitForNotion(String s, int maxLen) {
        if (s == null) return new String[] { "" };
        if (s.length() <= maxLen) return new String[] { s };

        java.util.ArrayList<String> parts = new java.util.ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            int end = Math.min(i + maxLen, s.length());
            // Essaie de reculer jusqu'au dernier '\n' dans cette fenêtre
            int lastNl = s.lastIndexOf('\n', end - 1);
            if (lastNl >= i && end - i > 200) { // évite des mini-chunks
                parts.add(s.substring(i, lastNl + 1));
                i = lastNl + 1;
            } else {
                parts.add(s.substring(i, end));
                i = end;
            }
        }
        return parts.toArray(new String[0]);
    }

    // Échappe correctement une chaîne pour JSON ("content": "...")
    private static String toJsonString(String s) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int)c));
                    else out.append(c);
                }
            }
        }
        out.append("\"");
        return out.toString();
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
