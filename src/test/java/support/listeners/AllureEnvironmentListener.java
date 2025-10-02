package support.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.tymo.api.services.PlayersService;
import org.testng.IExecutionListener;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class AllureEnvironmentListener implements IExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(AllureEnvironmentListener.class);

    @Override
    public void onExecutionFinish() {
        try {
            String resultsDir = System.getProperty("allure.results.directory",
                    "target/allure-results");
            Path dir = Path.of(resultsDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            Path envFile = dir.resolve("environment.properties");
            try (PrintWriter pw = new PrintWriter(new FileWriter(envFile.toFile()))) {
                pw.println("# Allure Environment");
                pw.println("Generated=" + LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                pw.println("Base URL=" + System.getProperty("app.baseUrl",
                        System.getenv().getOrDefault("BASE_URL", "http://3.68.165.45")));
                pw.println("Editor.Supervisor=" + PlayersService.defaultSupervisor());
                pw.println("Editor.Admin=" + PlayersService.defaultAdmin());
                pw.println("Threads=" + System.getProperty("threads", "3"));
                log.error("Allure environment.properties file created at: {}", envFile.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Failed to create environment.properties file for Allure report", e);
        }
    }
}
