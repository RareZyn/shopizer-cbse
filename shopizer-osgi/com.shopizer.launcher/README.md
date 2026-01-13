# Launcher Module (com.shopizer.launcher)

## Overview
The Launcher module initializes the Apache Felix OSGI framework and manages the lifecycle of all Shopizer bundles. It provides an interactive console for monitoring and controlling the OSGI platform.

## Architecture

### Module Type
- **Packaging**: JAR (not a bundle)
- **Main Class**: ShpizerOSGILauncher
- **Framework**: Apache Felix 7.0.5

### Dependencies
- Apache Felix Framework
- Felix Service Component Runtime (SCR)
- All Shopizer modules (as bundles to install)

## Components

### ShpizerOSGILauncher
Main launcher class that:
1. Initializes OSGI framework
2. Installs bundles in dependency order
3. Starts all bundles
4. Provides interactive console
5. Handles graceful shutdown

## Bundle Installation Order

The launcher installs bundles respecting their dependencies:

```
1. com.shopizer.common     (no dependencies)
2. com.shopizer.catalog    (depends on common)
3. com.shopizer.cart       (depends on common, catalog)
4. com.shopizer.order      (depends on common, cart, catalog)
5. com.shopizer.customer   (depends on common)
6. com.shopizer.merchant   (depends on common, catalog, order)
```

## Building the Platform

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Build All Modules
```bash
cd shopizer-osgi
mvn clean install
```

This will:
1. Build all bundles (common, catalog, cart, order, customer, merchant)
2. Create launcher JAR with dependencies
3. Generate executable: `com.shopizer.launcher-1.0.0-jar-with-dependencies.jar`

## Running the Platform

### Start the Platform
```bash
cd com.shopizer.launcher
java -jar target/com.shopizer.launcher-1.0.0-jar-with-dependencies.jar
```

### Expected Output
```
========================================
   Shopizer OSGI Platform Launcher
========================================
[OK] OSGI Framework started

Installing bundles...
[INSTALLED] com.shopizer.common
[INSTALLED] com.shopizer.catalog
[INSTALLED] com.shopizer.cart
[INSTALLED] com.shopizer.order
[INSTALLED] com.shopizer.customer
[INSTALLED] com.shopizer.merchant

Starting bundles...
[STARTED] com.shopizer.common
[STARTED] com.shopizer.catalog
[STARTED] com.shopizer.cart
[STARTED] com.shopizer.order
[STARTED] com.shopizer.customer
[STARTED] com.shopizer.merchant

========================================
   Shopizer OSGI Platform Running
========================================
Type 'exit' to shutdown the platform
Type 'status' to see bundle status
Type 'services' to list registered services
========================================

shopizer>
```

## Interactive Console Commands

### Available Commands

#### `status`
Display the status of all bundles:
```
shopizer> status

========================================
   Bundle Status
========================================
[ACTIVE] com.shopizer.common (ID: 1)
[ACTIVE] com.shopizer.catalog (ID: 2)
[ACTIVE] com.shopizer.cart (ID: 3)
[ACTIVE] com.shopizer.order (ID: 4)
[ACTIVE] com.shopizer.customer (ID: 5)
[ACTIVE] com.shopizer.merchant (ID: 6)
```

#### `services`
List all registered OSGI services:
```
shopizer> services

========================================
   Registered Services
========================================

com.shopizer.catalog:
  - com.shopizer.catalog.api.CatalogService

com.shopizer.cart:
  - com.shopizer.cart.api.CartService

com.shopizer.order:
  - com.shopizer.order.api.OrderService
  - com.shopizer.order.payment.PaymentProcessor

com.shopizer.customer:
  - com.shopizer.customer.api.CustomerService

com.shopizer.merchant:
  - com.shopizer.merchant.api.MerchantService
```

#### `help`
Display available commands:
```
shopizer> help

========================================
   Available Commands
========================================
status   - Show bundle status
services - List registered services
help     - Show this help message
exit     - Shutdown the platform
========================================
```

#### `exit` or `quit`
Shutdown the platform gracefully:
```
shopizer> exit

Shutting down Shopizer OSGI Platform...
Shutdown complete. Goodbye!
```

## Bundle States

The launcher tracks bundle lifecycle states:

- **INSTALLED** - Bundle installed but dependencies not resolved
- **RESOLVED** - Bundle dependencies resolved, ready to start
- **STARTING** - Bundle activator is being called
- **ACTIVE** - Bundle successfully started and running
- **STOPPING** - Bundle is being stopped
- **UNINSTALLED** - Bundle has been uninstalled

## Framework Configuration

### OSGI Framework Properties
```java
org.osgi.framework.storage = "felix-cache"
org.osgi.framework.storage.clean = "onFirstInit"
felix.log.level = "3" (INFO)
```

### Storage
- Framework state stored in `felix-cache` directory
- Cache cleaned on first initialization
- Persistent across restarts (if not cleaned)

## Project Structure

```
com.shopizer.launcher/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/shopizer/launcher/
        │       └── ShpizerOSGILauncher.java
        └── resources/
            └── (configuration files if needed)
```

## Troubleshooting

### Bundle Installation Failures

**Problem**: Bundle fails to install
```
[FAILED] Failed to install com.shopizer.catalog: Bundle not found
```

**Solution**: Ensure all bundles are built before running launcher:
```bash
mvn clean install
```

### Bundle Start Failures

**Problem**: Bundle fails to start
```
[FAILED] Failed to start com.shopizer.cart: Dependency not satisfied
```

**Solution**: Check bundle dependencies are installed and started first. The launcher handles this automatically.

### Missing Services

**Problem**: Services not registered
```
shopizer> services
(no services shown)
```

**Solution**:
1. Check bundle status - bundles must be ACTIVE
2. Check bundle activators are properly configured
3. Check OSGI imports/exports in bundle POMs

### Framework Won't Start

**Problem**: OSGI framework fails to initialize
```
Failed to start Shopizer OSGI Platform: FrameworkFactory not found
```

**Solution**: Ensure Apache Felix framework is in classpath (should be included in jar-with-dependencies)

## Development

### Adding New Bundles

To add a new bundle to the platform:

1. Create the bundle module
2. Add it to parent POM:
```xml
<modules>
    ...
    <module>com.shopizer.newmodule</module>
</modules>
```

3. Add dependency to launcher POM:
```xml
<dependency>
    <groupId>com.shopizer</groupId>
    <artifactId>com.shopizer.newmodule</artifactId>
    <version>${project.version}</version>
</dependency>
```

4. Update bundle installation order in ShpizerOSGILauncher:
```java
String[] bundleOrder = {
    "com.shopizer.common",
    "com.shopizer.catalog",
    // ... existing bundles
    "com.shopizer.newmodule"  // Add here
};
```

### Modifying Framework Configuration

Edit `createFramework()` method in ShpizerOSGILauncher:
```java
Map<String, String> config = new HashMap<>();
config.put("org.osgi.framework.storage", "felix-cache");
config.put("felix.log.level", "3"); // Change log level
config.put("your.custom.property", "value");
```

## Advanced Features

### Future Enhancements
- Configuration file support (properties/YAML)
- Remote console via Telnet/SSH
- Bundle hot-reload during development
- Web-based management console
- Health check endpoints
- Metrics and monitoring integration
- Cluster support for multiple instances
- Docker containerization

### Integration with Web Server
To integrate with a web server (Jetty/Tomcat):

1. Add web server bundles to dependencies
2. Install web server bundle after framework start
3. Register servlets as OSGI services
4. Access services via HTTP endpoints

Example:
```java
// Install Jetty bundle
Bundle jetty = context.installBundle("file:bundles/org.apache.felix.http.jetty.jar");
jetty.start();

// Register servlet
Hashtable<String, String> props = new Hashtable<>();
props.put("alias", "/api");
context.registerService(Servlet.class, new ApiServlet(), props);
```

## Component-Based Architecture Benefits

### Modularity
- Each module can be developed independently
- Clear separation of concerns
- Easy to understand and maintain

### Dynamic Services
- Services can be added/removed at runtime
- Loose coupling between modules
- Dependency injection via OSGI

### Versioning
- Multiple versions of same service can coexist
- Smooth upgrades without downtime
- Rollback capabilities

### Lifecycle Management
- Controlled startup and shutdown
- Proper resource cleanup
- Dependency resolution

## Performance Considerations

### Startup Time
- Sequential bundle installation and start
- Typical startup: 2-5 seconds
- Can be parallelized for large deployments

### Memory Usage
- Framework overhead: ~10-20 MB
- Each bundle: varies by functionality
- Typical total: 100-200 MB

### Bundle Resolution
- Resolved at install time
- Cached for performance
- Minimal runtime overhead

## Comparison: Monolithic vs OSGI

| Aspect | SpringBoot (Monolithic) | OSGI (Component-Based) |
|--------|------------------------|------------------------|
| Startup | Single application start | Framework + bundles |
| Dependencies | Compile-time classpath | Runtime service registry |
| Modularity | Package-level | Bundle-level |
| Updates | Full redeploy | Individual bundle update |
| Testing | Integration tests | Bundle isolation tests |
| Complexity | Lower | Higher (OSGI learning curve) |
| Flexibility | Less flexible | Highly flexible |

## Deployment

### Standalone Deployment
```bash
java -jar com.shopizer.launcher-1.0.0-jar-with-dependencies.jar
```

### Systemd Service (Linux)
```ini
[Unit]
Description=Shopizer OSGI Platform
After=network.target

[Service]
Type=simple
User=shopizer
WorkingDirectory=/opt/shopizer
ExecStart=/usr/bin/java -jar launcher.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

### Docker Container
```dockerfile
FROM openjdk:21-slim
WORKDIR /app
COPY target/com.shopizer.launcher-1.0.0-jar-with-dependencies.jar launcher.jar
EXPOSE 8080
CMD ["java", "-jar", "launcher.jar"]
```

## Logging

### Default Logging
- Uses SLF4J Simple implementation
- Logs to console
- INFO level by default

### Custom Logging
Replace slf4j-simple with preferred implementation:
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.14</version>
</dependency>
```

## Security

### Framework Security
- No default authentication
- Console accessible only locally
- Production: Implement authentication layer

### Bundle Security
- Java security manager can be enabled
- Permission-based access control
- Signed bundles for verification

## Support and Documentation

### OSGI Resources
- OSGI Alliance: https://www.osgi.org/
- Apache Felix: https://felix.apache.org/
- OSGI Specification: https://docs.osgi.org/

### Shopizer Modules
- See individual module READMEs
- Architecture documentation in shopizer-osgi/README.md
- Quick start guide in shopizer-osgi/QUICKSTART.md
