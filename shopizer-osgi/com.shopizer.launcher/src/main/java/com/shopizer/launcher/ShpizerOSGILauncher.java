package com.shopizer.launcher;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.util.*;

/**
 * Shopizer OSGI Framework Launcher
 * Initializes Apache Felix OSGI framework and installs all bundles
 */
public class ShpizerOSGILauncher {

    private static final String BUNDLE_DIR = "bundles";
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
        System.out.println("\nInstalling bundles...");

        // Define bundle installation order (respecting dependencies)
        String[] bundleOrder = {
            "com.shopizer.common",
            "com.shopizer.catalog",
            "com.shopizer.cart",
            "com.shopizer.order",
            "com.shopizer.customer",
            "com.shopizer.merchant"
        };

        List<Bundle> installedBundles = new ArrayList<>();

        // Install bundles
        for (String bundleName : bundleOrder) {
            try {
                String bundlePath = "file:target/" + bundleName + "-1.0.0.jar";
                Bundle bundle = context.installBundle(bundlePath);
                installedBundles.add(bundle);
                System.out.println("[INSTALLED] " + bundleName);
            } catch (BundleException e) {
                System.err.println("[FAILED] Failed to install " + bundleName + ": " + e.getMessage());
            }
        }

        System.out.println("\nStarting bundles...");

        // Start bundles
        for (Bundle bundle : installedBundles) {
            try {
                bundle.start();
                System.out.println("[STARTED] " + bundle.getSymbolicName());
            } catch (BundleException e) {
                System.err.println("[FAILED] Failed to start " + bundle.getSymbolicName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Interactive console for monitoring and control
     */
    private static void runConsole(BundleContext context) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.print("\nshopizer> ");
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
