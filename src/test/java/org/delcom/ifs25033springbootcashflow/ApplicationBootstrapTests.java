package org.delcom.ifs25033springbootcashflow;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;

class ApplicationBootstrapTests {
    @Test
    void mainLoadsEnvironmentAndStartsSpringApplication() throws IOException {
        Map<String, String> originalProperties = snapshotDotEnvProperties();
        String[] args = {"--spring.profiles.active=test"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            Ifs25033P5Pbo2026SpringbootCashflowApplication.main(args);

            springApplication.verify(() ->
                    SpringApplication.run(Ifs25033P5Pbo2026SpringbootCashflowApplication.class, args));
        } finally {
            restoreProperties(originalProperties);
        }
    }

    private static Map<String, String> snapshotDotEnvProperties() throws IOException {
        Map<String, String> originalProperties = new HashMap<>();
        Path envFile = Path.of(".env");
        if (!Files.isRegularFile(envFile)) {
            return originalProperties;
        }
        for (String line : Files.readAllLines(envFile)) {
            int separator = line.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            if (!originalProperties.containsKey(key)) {
                originalProperties.put(key, System.getProperty(key));
            }
        }
        return originalProperties;
    }

    private static void restoreProperties(Map<String, String> originalProperties) {
        originalProperties.forEach((key, value) -> {
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        });
    }
}
