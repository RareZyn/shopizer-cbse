# Database Configuration Guide for Shopizer OSGI

This guide explains how database configuration works in the Shopizer OSGI project.

---

## Overview

Unlike Spring Boot's single `application.properties` file, the OSGI architecture uses a **service-based configuration system** with:

1. **Properties File:** `database.properties` in the common module
2. **Configuration Service:** OSGI service for accessing configuration
3. **JPA Configuration:** Programmatic EntityManagerFactory setup
4. **Persistence Unit:** `persistence.xml` for entity mapping

---

## Configuration Files

### 1. Database Properties File

**Location:** `com.shopizer.common/src/main/resources/database.properties`

⚠️ **IMPORTANT:** This file is included in `.gitignore` and should **NOT be committed** to version control as it contains sensitive credentials.

This file contains all database connection settings, similar to Spring Boot's `application.properties`:

```properties
# ===============================
# SUPABASE POSTGRESQL DATABASE
# ===============================
javax.persistence.jdbc.url=jdbc:postgresql://db.tsnbtjrsqtyimzyjbwae.supabase.co:5432/postgres
javax.persistence.jdbc.user=postgres
javax.persistence.jdbc.password=3ZQXfTc8z9ICAuaI
javax.persistence.jdbc.driver=org.postgresql.Driver

# ===============================
# CONNECTION POOL (HikariCP)
# ===============================
hikari.maximum-pool-size=10
hikari.minimum-idle=5
hikari.idle-timeout=300000
hikari.connection-timeout=20000
hikari.max-lifetime=1200000

# ===============================
# JPA / HIBERNATE
# ===============================
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.hbm2ddl.auto=update
hibernate.show_sql=true
hibernate.format_sql=true
hibernate.use_sql_comments=true

# ===============================
# JWT CONFIGURATION
# ===============================
jwt.secret=your-256-bit-secret-key-change-this-in-production-minimum-32-characters
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

### 2. Persistence XML

**Location:** `com.shopizer.common/src/main/resources/META-INF/persistence.xml`

Defines the JPA persistence unit and entity mappings:

```xml
<persistence-unit name="shopizer-pu" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

    <!-- All entity classes listed here -->
    <class>com.shopizer.common.entity.Product</class>
    <class>com.shopizer.common.entity.Category</class>
    <!-- ... more entities ... -->
</persistence-unit>
```

---

## Configuration Architecture

### Service Components

```
DatabaseConfigService (Interface)
    ↓
DatabaseConfigServiceImpl (Implementation)
    ↓
JpaConfig (EntityManagerFactory Creator)
    ↓
CommonActivator (OSGI Bundle Activator)
```

### 1. DatabaseConfigService Interface

**Location:** `com.shopizer.common/src/main/java/com/shopizer/common/config/DatabaseConfigService.java`

Defines methods to access configuration values:

```java
public interface DatabaseConfigService {
    String getJdbcUrl();
    String getJdbcUser();
    String getJdbcPassword();
    String getHibernateDialect();
    Properties getJpaProperties();
    // ... more methods
}
```

### 2. DatabaseConfigServiceImpl

**Location:** `com.shopizer.common/src/main/java/com/shopizer/common/config/DatabaseConfigServiceImpl.java`

Loads properties from `database.properties` file:

```java
public class DatabaseConfigServiceImpl implements DatabaseConfigService {
    private final Properties properties;

    public DatabaseConfigServiceImpl() {
        // Loads database.properties from classpath
        loadProperties();
    }
}
```

### 3. JpaConfig

**Location:** `com.shopizer.common/src/main/java/com/shopizer/common/config/JpaConfig.java`

Creates EntityManagerFactory using the configuration:

```java
public class JpaConfig {
    public EntityManagerFactory createEntityManagerFactory() {
        Properties jpaProperties = configService.getJpaProperties();
        return Persistence.createEntityManagerFactory("shopizer-pu", jpaProperties);
    }
}
```

### 4. CommonActivator

**Location:** `com.shopizer.common/src/main/java/com/shopizer/common/CommonActivator.java`

Registers configuration services when bundle starts:

```java
public class CommonActivator implements BundleActivator {
    @Override
    public void start(BundleContext context) {
        // Create and register DatabaseConfigService
        DatabaseConfigService configService = new DatabaseConfigServiceImpl();
        context.registerService(DatabaseConfigService.class, configService, null);

        // Create and register EntityManagerFactory
        EntityManagerFactory emf = jpaConfig.createEntityManagerFactory();
        context.registerService(EntityManagerFactory.class, emf, null);
    }
}
```

---

## How It Works

### Startup Sequence

1. **Common Bundle Starts** → `CommonActivator.start()` is called
2. **Load Properties** → `DatabaseConfigServiceImpl` reads `database.properties`
3. **Create EntityManagerFactory** → `JpaConfig` creates JPA configuration
4. **Register Services** → Both services registered in OSGI Service Registry
5. **Other Bundles Access** → Catalog, Cart, Order modules can now use these services

### Service Access in Other Modules

Other modules (like Catalog, Customer, etc.) can access the configuration:

```java
// In any bundle's Activator
ServiceReference<DatabaseConfigService> ref =
    context.getServiceReference(DatabaseConfigService.class);
DatabaseConfigService configService = context.getService(ref);

// Use configuration
String jdbcUrl = configService.getJdbcUrl();
```

---

## Comparison: Spring Boot vs OSGI

| Aspect | Spring Boot | OSGI |
|--------|-------------|------|
| **Config File** | `application.properties` | `database.properties` |
| **Location** | `src/main/resources/` | `com.shopizer.common/src/main/resources/` |
| **Auto-Loading** | Yes (by Spring) | No (manual via service) |
| **Access Method** | `@Value` annotation | OSGI Service Registry |
| **Persistence** | `persistence.xml` optional | `persistence.xml` required |
| **EntityManagerFactory** | Auto-created by Spring | Manual creation in Activator |

---

## How to Change Database Settings

### Option 1: Update database.properties (Recommended)

⚠️ **Security Note:** The `database.properties` file is already in `.gitignore` to prevent committing sensitive credentials.

1. Edit `com.shopizer.common/src/main/resources/database.properties`
2. Change the database URL, username, password:
   ```properties
   javax.persistence.jdbc.url=jdbc:postgresql://your-host:5432/your-database
   javax.persistence.jdbc.user=your-username
   javax.persistence.jdbc.password=your-password
   ```
3. Rebuild the common module:
   ```bash
   cd shopizer-osgi/com.shopizer.common
   mvn clean install
   ```
4. Rebuild and restart the entire project:
   ```bash
   cd ..
   mvn clean install
   cd com.shopizer.launcher
   mvn exec:java
   ```

### Option 2: Environment Variables (Future Enhancement)

Modify `DatabaseConfigServiceImpl` to support environment variables:

```java
String jdbcUrl = System.getenv("SHOPIZER_DB_URL") != null
    ? System.getenv("SHOPIZER_DB_URL")
    : properties.getProperty("javax.persistence.jdbc.url");
```

---

## Troubleshooting

### Issue: "Unable to find database.properties"

**Solution:** Ensure the file exists at:
```
com.shopizer.common/src/main/resources/database.properties
```

Rebuild the module:
```bash
cd com.shopizer.common
mvn clean install
```

### Issue: "Failed to create EntityManagerFactory"

**Causes:**
1. Database server is not running
2. Incorrect connection URL, username, or password
3. PostgreSQL JDBC driver not in classpath

**Solution:**
1. Verify database is accessible:
   ```bash
   psql -h db.tsnbtjrsqtyimzyjbwae.supabase.co -U postgres -d postgres
   ```
2. Check `database.properties` settings
3. Verify PostgreSQL dependency in `pom.xml`

### Issue: Bundle fails to start

**Solution:** Check console output for error messages. Common issues:
- Missing persistence.xml
- Entity classes not listed in persistence.xml
- Database connection timeout

---

## Adding New Configuration Properties

1. **Add property to database.properties:**
   ```properties
   custom.property=value
   ```

2. **Add getter to DatabaseConfigService interface:**
   ```java
   String getCustomProperty();
   ```

3. **Implement in DatabaseConfigServiceImpl:**
   ```java
   @Override
   public String getCustomProperty() {
       return properties.getProperty("custom.property");
   }
   ```

4. **Rebuild common module**

---

## Best Practices

1. **Never commit sensitive credentials** - `database.properties` is already in `.gitignore` to protect your credentials
2. **Use environment variables in production** - Override properties with env vars for deployment
3. **Use connection pooling** - Already configured with HikariCP settings
4. **Set appropriate pool sizes** - Adjust based on your application load
5. **Use update for development** - `hibernate.hbm2ddl.auto=update`
6. **Use validate for production** - `hibernate.hbm2ddl.auto=validate`

### .gitignore Configuration

The following entries are already in `.gitignore`:
```
shopizer-osgi/com.shopizer.common/src/main/resources/database.properties
shopizer-osgi/com.shopizer.common/target/classes/database.properties
```

This ensures your database credentials are **never committed** to version control.

---

## File Structure Summary

```
com.shopizer.common/
├── src/main/
│   ├── java/com/shopizer/common/
│   │   ├── config/
│   │   │   ├── DatabaseConfigService.java         (Interface)
│   │   │   ├── DatabaseConfigServiceImpl.java     (Implementation)
│   │   │   └── JpaConfig.java                     (JPA Setup)
│   │   ├── CommonActivator.java                   (Bundle Activator)
│   │   ├── entity/                                (JPA Entities)
│   │   └── ...
│   └── resources/
│       ├── database.properties                    (Database Config)
│       └── META-INF/
│           └── persistence.xml                    (JPA Persistence Unit)
└── pom.xml                                        (Maven Config)
```

---

## Summary

Your database configuration is now set up in the OSGI structure:

✅ **database.properties** - Contains all database settings
✅ **DatabaseConfigService** - OSGI service for accessing config
✅ **JpaConfig** - Creates EntityManagerFactory
✅ **CommonActivator** - Registers services on bundle start
✅ **persistence.xml** - Defines JPA persistence unit

The configuration is centralized in the **common module** and accessible to all other bundles through the OSGI Service Registry.

---

**Last Updated:** January 13, 2026
**Module:** com.shopizer.common
**Configuration Status:** ✅ Fully Operational
