// AuditDbConfig.java - UPDATED
package com.sein_gar_har.DBConfig;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.sein_gar_har.RepositoryAudit",
        entityManagerFactoryRef = "auditEntityManagerFactory",
        transactionManagerRef = "auditTransactionManager"
)
public class AuditDbConfig {

    @Value("${spring.datasource.audit.jdbc-url}")
    private String auditUrl;

    @Value("${spring.datasource.audit.username}")
    private String auditUsername;

    @Value("${spring.datasource.audit.password}")
    private String auditPassword;

    @Bean(name = "auditDataSource")
    public DataSource auditDataSource() {
        return DataSourceBuilder.create()
                .url(auditUrl)
                .username(auditUsername)
                .password(auditPassword)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Bean(name = "auditEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(
            @Qualifier("auditDataSource") DataSource auditDataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(auditDataSource);
        // ONLY scan audit entities
        em.setPackagesToScan("com.sein_gar_har.auditEntity");

        // Give this EMF a unique persistence unit name
        em.setPersistenceUnitName("AUDIT_PU");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties props = new Properties();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        // Add these to avoid conflicts
        props.put("hibernate.transaction.coordinator_class", "jdbc");
        props.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        em.setJpaProperties(props);

        return em;
    }

    @Bean(name = "auditTransactionManager")
    public PlatformTransactionManager auditTransactionManager(
            @Qualifier("auditEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}