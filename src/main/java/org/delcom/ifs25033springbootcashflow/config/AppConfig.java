package org.delcom.ifs25033springbootcashflow.config;

import org.delcom.ifs25033springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25033springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25033springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25033springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25033springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25033springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25033springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25033springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25033springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25033springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@Configuration
public class AppConfig {
    @Bean
    public IUserRepository userRepository(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(jdbcTemplate);
    }

    @Bean
    public ITransactionRepository transactionRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcTransactionRepository(jdbcTemplate);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository repository) {
        return new AuthUseCase(repository);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository repository) {
        return new FinanceUseCase(repository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository repository) {
        return new UserUseCase(repository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir:uploads}") String uploadDir) {
        return new FileStorage(uploadDir);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(AuthUseCase authUseCase,
            @Value("${app.root-username:admin}") String username,
            @Value("${app.root-password:admin}") String password) {
        return args -> {
            try {
                authUseCase.ensureRootAdmin(username, password);
            } catch (ValidationException exception) {
                throw new IllegalStateException("Konfigurasi admin awal tidak valid", exception);
            }
        };
    }

    @Bean
    public WebMvcConfigurer uploadResourceConfigurer(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String location = fileStorage.root().toUri().toString();
                if (!location.endsWith("/")) {
                    location += "/";
                }
                registry.addResourceHandler("/uploads/**").addResourceLocations(location);
            }
        };
    }
}
