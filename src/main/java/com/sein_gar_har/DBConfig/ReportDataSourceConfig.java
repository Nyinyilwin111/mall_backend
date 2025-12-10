package com.sein_gar_har.DBConfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ReportDataSourceConfig {

    // Use the existing auditDataSource bean
    @Bean(name = "auditJdbcTemplate")
    public JdbcTemplate auditJdbcTemplate(@Qualifier("auditDataSource") DataSource auditDataSource) {
        return new JdbcTemplate(auditDataSource);
    }

    @Bean(name = "auditNamedJdbcTemplate")
    public NamedParameterJdbcTemplate auditNamedJdbcTemplate(@Qualifier("auditDataSource") DataSource auditDataSource) {
        return new NamedParameterJdbcTemplate(auditDataSource);
    }
}