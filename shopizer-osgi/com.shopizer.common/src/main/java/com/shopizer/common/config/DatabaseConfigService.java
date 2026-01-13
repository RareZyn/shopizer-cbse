package com.shopizer.common.config;

import java.util.Properties;

/**
 * Service interface for database configuration
 * Provides access to database connection settings, JPA/Hibernate properties, and JWT configuration
 */
public interface DatabaseConfigService {

    /**
     * Get JDBC URL for database connection
     * @return JDBC connection URL
     */
    String getJdbcUrl();

    /**
     * Get database username
     * @return Database username
     */
    String getJdbcUser();

    /**
     * Get database password
     * @return Database password
     */
    String getJdbcPassword();

    /**
     * Get JDBC driver class name
     * @return JDBC driver class name
     */
    String getJdbcDriver();

    /**
     * Get Hibernate dialect
     * @return Hibernate dialect class name
     */
    String getHibernateDialect();

    /**
     * Get Hibernate DDL auto strategy
     * @return DDL auto strategy (update, create, create-drop, validate, none)
     */
    String getHibernateDdlAuto();

    /**
     * Check if SQL queries should be shown in logs
     * @return true if SQL should be shown
     */
    boolean isShowSql();

    /**
     * Check if SQL should be formatted in logs
     * @return true if SQL should be formatted
     */
    boolean isFormatSql();

    /**
     * Get JWT secret key
     * @return JWT secret key
     */
    String getJwtSecret();

    /**
     * Get JWT token expiration time in milliseconds
     * @return JWT expiration time
     */
    long getJwtExpiration();

    /**
     * Get JWT refresh token expiration time in milliseconds
     * @return JWT refresh token expiration time
     */
    long getJwtRefreshExpiration();

    /**
     * Get HikariCP maximum pool size
     * @return Maximum pool size
     */
    int getHikariMaximumPoolSize();

    /**
     * Get HikariCP minimum idle connections
     * @return Minimum idle connections
     */
    int getHikariMinimumIdle();

    /**
     * Get all database properties
     * @return Properties object with all configuration
     */
    Properties getAllProperties();

    /**
     * Get JPA properties for EntityManagerFactory
     * @return Properties for JPA configuration
     */
    Properties getJpaProperties();
}
