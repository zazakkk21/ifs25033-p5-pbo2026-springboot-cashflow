package org.delcom.ifs25033springbootcashflow.config;

import org.delcom.ifs25033springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25033springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25033springbootcashflow.usecase.AuthUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigCoverageTests {
    @TempDir
    Path tempDir;

    @Test
    void rootAdminRunnerSurfacesInvalidConfigurationAndSeedsValidConfiguration() throws Exception {
        AppConfig config = new AppConfig();
        ApplicationArguments arguments = new DefaultApplicationArguments();
        AuthUseCase invalidAuth = new AuthUseCase(new UserRepository());
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> config.seedRootAdmin(invalidAuth, "admin", "123").run(arguments));
        assertEquals("Konfigurasi admin awal tidak valid", exception.getMessage());

        UserRepository users = new UserRepository();
        AuthUseCase validAuth = new AuthUseCase(users);
        config.seedRootAdmin(validAuth, "admin", "admin-password").run(arguments);
        assertEquals("admin", users.findByUsername("admin").orElseThrow().getUsername());
    }

    @Test
    void uploadResourceConfigurerNormalizesResourceLocationUris() throws Exception {
        Path nonexistentPath = tempDir.resolve("not-created");
        assertResourceLocationHasTrailingSlash(nonexistentPath);

        Path directory = Files.createDirectory(tempDir.resolve("created"));
        assertResourceLocationHasTrailingSlash(directory);
    }

    private static void assertResourceLocationHasTrailingSlash(Path root) {
        FileStorage storage = new FileStorage(root.toString());
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);

        new AppConfig().uploadResourceConfigurer(storage).addResourceHandlers(registry);

        String location = storage.root().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        verify(registration).addResourceLocations(location);
    }
}
