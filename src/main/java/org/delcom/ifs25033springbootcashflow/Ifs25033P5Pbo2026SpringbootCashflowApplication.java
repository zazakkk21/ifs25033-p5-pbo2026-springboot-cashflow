package org.delcom.ifs25033springbootcashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

@SpringBootApplication
@Theme("cashflow")
public class Ifs25033P5Pbo2026SpringbootCashflowApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(Ifs25033P5Pbo2026SpringbootCashflowApplication.class, args);
    }

}
