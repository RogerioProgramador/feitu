package com.feitu.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataMigrator implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public DataMigrator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Garante que linhas legadas (v1) recebam valores default para colunas v2
        jdbc.update("UPDATE tarefas SET tipo = 'PONTUAL' WHERE tipo IS NULL");
        jdbc.update("UPDATE tarefas SET concluida = FALSE WHERE concluida IS NULL");
        jdbc.update("UPDATE tarefas SET criado_em = CURRENT_TIMESTAMP WHERE criado_em IS NULL");
    }
}
