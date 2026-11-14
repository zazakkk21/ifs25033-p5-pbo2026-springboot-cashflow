package org.delcom.ifs25033springbootcashflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme("cashflow")
@Meta(name = "description",
        content = "Aplikasi Cashflow untuk mengelola pemasukan, pengeluaran, dan riwayat transaksi keuangan secara mudah dan terorganisir.")
public class Ifs25033P5Pbo2026SpringbootCashflowApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(Ifs25033P5Pbo2026SpringbootCashflowApplication.class, args);
    }

}
