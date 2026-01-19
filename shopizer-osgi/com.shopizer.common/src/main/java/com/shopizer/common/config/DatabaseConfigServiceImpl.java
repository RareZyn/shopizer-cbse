package com.shopizer.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Implementation of DatabaseConfigService
 * Loads database configuration from database.properties file
 */
public class DatabaseConfigServiceImpl implements DatabaseConfigService {

    private final Properties properties;

    public DatabaseConfigServiceImpl() {
        this.properties = new Properties();
        loadProperties();
    }

    /**
     * Load properties from database.properties file
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("META-INF/database.properties")) {

            if (input == null) {
                throw new RuntimeException("Unable to find database.properties");
            }

            properties.load(input);
            System.out.println("Database configuration loaded successfully");

        } catch (IOException ex) {
            throw new RuntimeException("Failed to load database configuration", ex);
        }
    }

    @Override
    public String getJdbcUrl() {
        return properties.getProperty("javax.persistence.jdbc.url");
    }

    @Override
    public String getJdbcUser() {
        return properties.getProperty("javax.persistence.jdbc.user");
    }

    @Override
    public String getJdbcPassword() {
        return properties.getProperty("javax.persistence.jdbc.password");
    }

    @Override
    public String getJdbcDriver() {
        return properties.getProperty("javax.persistence.jdbc.driver");
    }

    @Override
    public String getHibernateDialect() {
        return properties.getProperty("hibernate.dialect");
    }

    @Override
    public String getHibernateDdlAuto() {
        return properties.getProperty("hibernate.hbm2ddl.auto", "update");
    }

    @Override
    public boolean isShowSql() {
        return Boolean.parseBoolean(properties.getProperty("hibernate.show_sql", "true"));
    }

    @Override
    public boolean isFormatSql() {
        return Boolean.parseBoolean(properties.getProperty("hibernate.format_sql", "true"));
    }

    @Override
    public String getJwtSecret() {
        return properties.getProperty("jwt.secret");
    }

    @Override
    public long getJwtExpiration() {
        return Long.parseLong(properties.getProperty("jwt.expiration", "86400000"));
    }

    @Override
    public long getJwtRefreshExpiration() {
        return Long.parseLong(properties.getProperty("jwt.refresh-expiration", "604800000"));
    }

    @Override
    public int getHikariMaximumPoolSize() {
        return Integer.parseInt(properties.getProperty("hikari.maximum-pool-size", "10"));
    }

    @Override
    public int getHikariMinimumIdle() {
        return Integer.parseInt(properties.getProperty("hikari.minimum-idle", "5"));
    }

    @Override
    public Properties getAllProperties() {
        return (Properties) properties.clone();
    }

    @Override
    public Properties getJpaProperties() {
        Properties jpaProps = new Properties();

        // JDBC connection properties
        jpaProps.setProperty("javax.persistence.jdbc.url", getJdbcUrl());
        jpaProps.setProperty("javax.persistence.jdbc.user", getJdbcUser());
        jpaProps.setProperty("javax.persistence.jdbc.password", getJdbcPassword());
        jpaProps.setProperty("javax.persistence.jdbc.driver", getJdbcDriver());

        // Hibernate properties
        jpaProps.setProperty("hibernate.dialect", getHibernateDialect());
        jpaProps.setProperty("hibernate.hbm2ddl.auto", getHibernateDdlAuto());
        jpaProps.setProperty("hibernate.show_sql", String.valueOf(isShowSql()));
        jpaProps.setProperty("hibernate.format_sql", String.valueOf(isFormatSql()));

        // Additional Hibernate properties
        if (properties.containsKey("hibernate.use_sql_comments")) {
            jpaProps.setProperty("hibernate.use_sql_comments",
                properties.getProperty("hibernate.use_sql_comments"));
        }

        // HikariCP properties
        jpaProps.setProperty("hibernate.hikari.maximumPoolSize",
            String.valueOf(getHikariMaximumPoolSize()));
        jpaProps.setProperty("hibernate.hikari.minimumIdle",
            String.valueOf(getHikariMinimumIdle()));

        if (properties.containsKey("hikari.idle-timeout")) {
            jpaProps.setProperty("hibernate.hikari.idleTimeout",
                properties.getProperty("hikari.idle-timeout"));
        }
        if (properties.containsKey("hikari.connection-timeout")) {
            jpaProps.setProperty("hibernate.hikari.connectionTimeout",
                properties.getProperty("hikari.connection-timeout"));
        }
        if (properties.containsKey("hikari.max-lifetime")) {
            jpaProps.setProperty("hibernate.hikari.maxLifetime",
                properties.getProperty("hikari.max-lifetime"));
        }

        return jpaProps;
    }
}
