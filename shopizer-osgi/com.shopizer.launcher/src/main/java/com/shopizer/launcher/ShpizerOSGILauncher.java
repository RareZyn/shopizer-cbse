package com.shopizer.launcher;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import java.util.*;

/**
 * Shopizer OSGI Framework Launcher
 * Initializes Apache Felix OSGI framework and installs all bundles
 */
public class ShpizerOSGILauncher {

    private static Framework framework;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   Shopizer OSGI Platform Launcher");
        System.out.println("========================================");

        try {
            // Start OSGI Framework
            framework = createFramework();
            framework.start();
            System.out.println("[OK] OSGI Framework started");

            // Get bundle context
            BundleContext context = framework.getBundleContext();

            // Install and start bundles in dependency order
            installAndStartBundles(context);

            System.out.println("========================================");
            System.out.println("   Shopizer OSGI Platform Running");
            System.out.println("========================================");
            System.out.println("Type 'exit' to shutdown the platform");
            System.out.println("Type 'status' to see bundle status");
            System.out.println("Type 'services' to list registered services");
            System.out.println("========================================");

            // Interactive console
            runConsole(context);

            // Shutdown
            System.out.println("\nShutting down Shopizer OSGI Platform...");
            framework.stop();
            framework.waitForStop(0);

            System.out.println("Shutdown complete. Goodbye!");

        } catch (Exception e) {
            System.err.println("Failed to start Shopizer OSGI Platform: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Create and configure OSGI framework
     */
    private static Framework createFramework() {
        // Load framework factory
        FrameworkFactory factory = ServiceLoader.load(FrameworkFactory.class).iterator().next();

        // Configure framework properties
        Map<String, String> config = new HashMap<>();
        config.put("org.osgi.framework.storage", "felix-cache");
        config.put("org.osgi.framework.storage.clean", "onFirstInit");
        config.put("felix.log.level", "3"); // INFO level

        // Create framework
        return factory.newFramework(config);
    }

    /**
     * Install and start bundles in dependency order
     */
    private static void installAndStartBundles(BundleContext context) {
        System.out.println("\nInstalling third-party dependency bundles...");

        // Install third-party OSGI bundles first
        List<Bundle> dependencyBundles = installDependencyBundles(context);

        System.out.println("\nInstalling application bundles...");

        // Define bundle installation order (respecting dependencies)
        String[] bundleOrder = {
            "com.shopizer.common",
            "com.shopizer.catalog",
            "com.shopizer.cart",
            "com.shopizer.order",
            "com.shopizer.customer",
            "com.shopizer.merchant",
            "com.shopizer.rest"  // REST API module (must be last)
        };

        List<Bundle> installedBundles = new ArrayList<>();

        // Install bundles
        for (String bundleName : bundleOrder) {
            try {
                // Build path relative to parent directory (shopizer-osgi)
                String bundlePath = "file:../" + bundleName + "/target/" + bundleName + "-1.0.0.jar";
                Bundle bundle = context.installBundle(bundlePath);
                installedBundles.add(bundle);
                System.out.println("[INSTALLED] " + bundleName);
            } catch (BundleException e) {
                System.err.println("[FAILED] Failed to install " + bundleName + ": " + e.getMessage());
            }
        }

        System.out.println("\nStarting bundles...");

        // Start dependency bundles first
        // Start SPI Fly bundle first (if present) as it provides the ServiceLoader extender
        for (Bundle bundle : dependencyBundles) {
            if (bundle.getSymbolicName() != null && bundle.getSymbolicName().contains("spifly")) {
                try {
                    bundle.start();
                    System.out.println("[STARTED] " + bundle.getSymbolicName());
                } catch (BundleException e) {
                    System.err.println("[FAILED] Failed to start " + bundle.getSymbolicName() + ": " + e.getMessage());
                }
            }
        }

        // Start other dependency bundles
        for (Bundle bundle : dependencyBundles) {
            if (bundle.getSymbolicName() == null || !bundle.getSymbolicName().contains("spifly")) {
                try {
                    bundle.start();
                    System.out.println("[STARTED] " + bundle.getSymbolicName());
                } catch (BundleException e) {
                    // Some bundles are fragments and cannot be started
                    if (e.getType() != BundleException.INVALID_OPERATION) {
                        System.err.println("[FAILED] Failed to start " + bundle.getSymbolicName() + ": " + e.getMessage());
                    }
                }
            }
        }

        // Start application bundles
        for (Bundle bundle : installedBundles) {
            try {
                bundle.start();
                System.out.println("[STARTED] " + bundle.getSymbolicName());
            } catch (BundleException e) {
                System.err.println("[FAILED] Failed to start " + bundle.getSymbolicName() + ": " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Cause: ");
                    e.getCause().printStackTrace();
                }
            }
        }
    }

    /**
     * Install third-party dependency bundles from Maven repository
     */
    private static List<Bundle> installDependencyBundles(BundleContext context) {
        List<Bundle> bundles = new ArrayList<>();

        // Get user home directory for Maven local repository
        String userHome = System.getProperty("user.home");
        String mavenRepo = userHome + "/.m2/repository/";

        // Define Maven dependencies to install as OSGI bundles
        String[][] dependencies = {
            // Jakarta Persistence API
            {"jakarta/persistence/jakarta.persistence-api/3.1.0", "jakarta.persistence-api-3.1.0.jar"},

            // Jakarta Servlet API (required by REST module)
            {"jakarta/servlet/jakarta.servlet-api/6.0.0", "jakarta.servlet-api-6.0.0.jar"},

            // ASM library (required by Aries SPI Fly)
            {"org/ow2/asm/asm/9.7", "asm-9.7.jar"},
            {"org/ow2/asm/asm-commons/9.7", "asm-commons-9.7.jar"},
            {"org/ow2/asm/asm-tree/9.7", "asm-tree-9.7.jar"},
            {"org/ow2/asm/asm-analysis/9.7", "asm-analysis-9.7.jar"},
            {"org/ow2/asm/asm-util/9.7", "asm-util-9.7.jar"},

            // OSGI ServiceLoader Mediator (required by SLF4J)
            {"org/apache/aries/spifly/org.apache.aries.spifly.dynamic.bundle/1.3.7", "org.apache.aries.spifly.dynamic.bundle-1.3.7.jar"},

            // SLF4J API (logging)
            {"org/slf4j/slf4j-api/2.0.9", "slf4j-api-2.0.9.jar"},

            // SLF4J Simple Implementation
            {"org/slf4j/slf4j-simple/2.0.9", "slf4j-simple-2.0.9.jar"},
        };

        for (String[] dep : dependencies) {
            try {
                String bundlePath = "file:///" + mavenRepo + dep[0] + "/" + dep[1];
                bundlePath = bundlePath.replace("\\", "/");
                Bundle bundle = context.installBundle(bundlePath);
                bundles.add(bundle);
                System.out.println("[INSTALLED] " + dep[1]);
            } catch (BundleException e) {
                System.err.println("[WARN] Failed to install " + dep[1] + ": " + e.getMessage());
                System.err.println("       Bundle path: file:///" + mavenRepo + dep[0] + "/" + dep[1]);
            }
        }

        return bundles;
    }

    /**
     * Interactive console for monitoring and control
     */
    private static void runConsole(BundleContext context) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.print("\nshopizer> ");
            if (!scanner.hasNextLine()) {
                // No input available (non-interactive mode), keep running
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            String command = scanner.nextLine().trim();

            switch (command.toLowerCase()) {
                case "exit":
                case "quit":
                    running = false;
                    break;

                case "status":
                    showBundleStatus(context);
                    break;

                case "services":
                    showRegisteredServices(context);
                    break;

                case "help":
                    showHelp();
                    break;

                case "":
                    // Ignore empty input
                    break;

                default:
                    System.out.println("Unknown command: " + command);
                    System.out.println("Type 'help' for available commands");
            }
        }

        scanner.close();
    }

    /**
     * Display bundle status
     */
    private static void showBundleStatus(BundleContext context) {
        System.out.println("\n========================================");
        System.out.println("   Bundle Status");
        System.out.println("========================================");

        Bundle[] bundles = context.getBundles();
        for (Bundle bundle : bundles) {
            String status = getBundleStatus(bundle.getState());
            System.out.printf("[%s] %s (ID: %d)%n",
                status,
                bundle.getSymbolicName(),
                bundle.getBundleId());
        }
    }

    /**
     * Display registered services
     */
    private static void showRegisteredServices(BundleContext context) {
        System.out.println("\n========================================");
        System.out.println("   Registered Services");
        System.out.println("========================================");

        Bundle[] bundles = context.getBundles();
        for (Bundle bundle : bundles) {
            var registeredServices = bundle.getRegisteredServices();
            if (registeredServices != null && registeredServices.length > 0) {
                System.out.println("\n" + bundle.getSymbolicName() + ":");
                for (var serviceRef : registeredServices) {
                    String[] objectClass = (String[]) serviceRef.getProperty("objectClass");
                    for (String className : objectClass) {
                        System.out.println("  - " + className);
                    }
                }
            }
        }
    }

    /**
     * Display help information
     */
    private static void showHelp() {
        System.out.println("\n========================================");
        System.out.println("   Available Commands");
        System.out.println("========================================");
        System.out.println("status   - Show bundle status");
        System.out.println("services - List registered services");
        System.out.println("help     - Show this help message");
        System.out.println("exit     - Shutdown the platform");
        System.out.println("========================================");
    }

    /**
     * Convert bundle state to readable string
     */
    private static String getBundleStatus(int state) {
        return switch (state) {
            case Bundle.INSTALLED -> "INSTALLED";
            case Bundle.RESOLVED -> "RESOLVED";
            case Bundle.STARTING -> "STARTING";
            case Bundle.ACTIVE -> "ACTIVE";
            case Bundle.STOPPING -> "STOPPING";
            case Bundle.UNINSTALLED -> "UNINSTALLED";
            default -> "UNKNOWN";
        };
    }
}
