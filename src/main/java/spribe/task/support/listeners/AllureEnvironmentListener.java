package spribe.task.support.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import spribe.task.common.env.ConfigFactoryProvider;
import spribe.task.common.env.TestConfig;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TestNG listener that generates environment.properties file for Allure report.
 * Executes after all tests are finished.
 */
public class AllureEnvironmentListener implements IExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(AllureEnvironmentListener.class);

    @Override
    public void onExecutionFinish() {
        try {
            TestConfig config = ConfigFactoryProvider.config();
            String resultsDir = config.allureResultsDirectory();
            Path dir = Path.of(resultsDir);
            
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path envFile = dir.resolve("environment.properties");
            try (PrintWriter pw = new PrintWriter(new FileWriter(envFile.toFile()))) {
                pw.println("# Allure Environment");
                pw.println("Generated=" + LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                pw.println("Environment=" + System.getProperty("env", "prod").toUpperCase());
                pw.println("Base URL=" + config.baseUrl());
                pw.println("Editor.Supervisor=" + config.supervisorLogin());
                pw.println("Editor.Admin=" + config.adminLogin());
                pw.println("Threads=" + config.threadCount());
                pw.println("API Timeout=" + config.apiTimeout() + "ms");
                
                log.info("Allure environment.properties file created at: {}", envFile.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Failed to create environment.properties file for Allure report", e);
        }
    }
}

