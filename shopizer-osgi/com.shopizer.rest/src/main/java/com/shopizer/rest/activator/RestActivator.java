package com.shopizer.rest.activator;

import com.shopizer.cart.api.CartService;
import com.shopizer.catalog.api.CatalogService;
import com.shopizer.customer.api.CustomerService;
import com.shopizer.merchant.api.MerchantService;
import com.shopizer.order.api.OrderService;
import com.shopizer.rest.servlet.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGI Bundle Activator for REST API Module
 *
 * Starts Jetty web server on port 8080 and registers REST endpoints
 * for all Shopizer OSGI services.
 */
public class RestActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(RestActivator.class);
    private static final int HTTP_PORT = 8080;

    private Server jettyServer;
    private BundleContext context;

    // Service trackers to monitor OSGI services
    private ServiceTracker<CatalogService, CatalogService> catalogTracker;
    private ServiceTracker<CartService, CartService> cartTracker;
    private ServiceTracker<OrderService, OrderService> orderTracker;
    private ServiceTracker<CustomerService, CustomerService> customerTracker;
    private ServiceTracker<MerchantService, MerchantService> merchantTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        System.out.println("[REST] Starting Shopizer REST API Bundle...");

        try {
            // Wait for all required services to be available
            startServiceTrackers();

            // Wait a bit for services to register
            Thread.sleep(1000);

            // Start Jetty server
            startJettyServer();

            System.out.println("[REST] Shopizer REST API Bundle started successfully");
            System.out.println("[REST] REST API available at: http://localhost:" + HTTP_PORT + "/api/v1");
            System.out.println("[REST] Web UI available at: http://localhost:" + HTTP_PORT + "/");
            printEndpointSummary();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Shopizer REST API Bundle...");

        // Stop Jetty server
        if (jettyServer != null && jettyServer.isRunning()) {
            jettyServer.stop();
            logger.info("Jetty server stopped");
        }

        // Close service trackers
        if (catalogTracker != null) catalogTracker.close();
        if (cartTracker != null) cartTracker.close();
        if (orderTracker != null) orderTracker.close();
        if (customerTracker != null) customerTracker.close();
        if (merchantTracker != null) merchantTracker.close();

        logger.info("Shopizer REST API Bundle stopped");
    }

    private void startServiceTrackers() {
        // Track CatalogService
        catalogTracker = new ServiceTracker<>(context, CatalogService.class, null);
        catalogTracker.open();

        // Track CartService
        cartTracker = new ServiceTracker<>(context, CartService.class, null);
        cartTracker.open();

        // Track OrderService
        orderTracker = new ServiceTracker<>(context, OrderService.class, null);
        orderTracker.open();

        // Track CustomerService
        customerTracker = new ServiceTracker<>(context, CustomerService.class, null);
        customerTracker.open();

        // Track MerchantService
        merchantTracker = new ServiceTracker<>(context, MerchantService.class, null);
        merchantTracker.open();

        logger.info("Service trackers started");
    }

    private void startJettyServer() throws Exception {
        // Create Jetty server
        jettyServer = new Server(HTTP_PORT);

        // Create servlet context
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        // Register index page at root
        registerIndexPage(contextHandler);

        // Register servlets for each service
        registerCatalogEndpoints(contextHandler);
        registerCartEndpoints(contextHandler);
        registerOrderEndpoints(contextHandler);
        registerCustomerEndpoints(contextHandler);
        registerMerchantEndpoints(contextHandler);

        // Register Swagger documentation endpoint
        registerSwaggerEndpoint(contextHandler);

        // Add context handler to server
        jettyServer.setHandler(contextHandler);

        // Start server
        jettyServer.start();
        System.out.println("[REST] Jetty server started on port " + HTTP_PORT);
        System.out.println("[REST] Jetty server is running: " + jettyServer.isRunning());
        System.out.println("[REST] Jetty server is started: " + jettyServer.isStarted());
    }

    private void registerIndexPage(ServletContextHandler context) {
        ServletHolder indexServlet = new ServletHolder(new IndexServlet());
        context.addServlet(indexServlet, "/");
        System.out.println("[REST] Registered Index page: http://localhost:" + HTTP_PORT + "/");
    }

    private void registerCatalogEndpoints(ServletContextHandler context) {
        CatalogService catalogService = catalogTracker.getService();
        if (catalogService != null) {
            // Product endpoints
            ServletHolder productServlet = new ServletHolder(new CatalogServlet(catalogService));
            context.addServlet(productServlet, "/api/v1/products/*");

            // Category endpoints
            ServletHolder categoryServlet = new ServletHolder(new CatalogServlet(catalogService));
            context.addServlet(categoryServlet, "/api/v1/categories/*");

            logger.info("Registered Catalog endpoints: /api/v1/products/*, /api/v1/categories/*");
        } else {
            logger.warn("CatalogService not available, skipping endpoint registration");
        }
    }

    private void registerCartEndpoints(ServletContextHandler context) {
        CartService cartService = cartTracker.getService();
        if (cartService != null) {
            ServletHolder cartServlet = new ServletHolder(new CartServlet(cartService));
            context.addServlet(cartServlet, "/api/v1/cart/*");

            logger.info("Registered Cart endpoints: /api/v1/cart/*");
        } else {
            logger.warn("CartService not available, skipping endpoint registration");
        }
    }

    private void registerOrderEndpoints(ServletContextHandler context) {
        OrderService orderService = orderTracker.getService();
        if (orderService != null) {
            ServletHolder orderServlet = new ServletHolder(new OrderServlet(orderService));
            context.addServlet(orderServlet, "/api/v1/orders/*");

            logger.info("Registered Order endpoints: /api/v1/orders/*");
        } else {
            logger.warn("OrderService not available, skipping endpoint registration");
        }
    }

    private void registerCustomerEndpoints(ServletContextHandler context) {
        CustomerService customerService = customerTracker.getService();
        if (customerService != null) {
            ServletHolder customerServlet = new ServletHolder(new CustomerServlet(customerService));
            context.addServlet(customerServlet, "/api/v1/customers/*");

            logger.info("Registered Customer endpoints: /api/v1/customers/*");
        } else {
            logger.warn("CustomerService not available, skipping endpoint registration");
        }
    }

    private void registerMerchantEndpoints(ServletContextHandler context) {
        MerchantService merchantService = merchantTracker.getService();
        if (merchantService != null) {
            ServletHolder merchantServlet = new ServletHolder(new MerchantServlet(merchantService));
            context.addServlet(merchantServlet, "/api/v1/merchants/*");

            logger.info("Registered Merchant endpoints: /api/v1/merchants/*");
        } else {
            logger.warn("MerchantService not available, skipping endpoint registration");
        }
    }

    private void registerSwaggerEndpoint(ServletContextHandler context) {
        ServletHolder swaggerServlet = new ServletHolder(new SwaggerServlet());
        context.addServlet(swaggerServlet, "/api/docs/*");

        logger.info("Registered Swagger UI: http://localhost:" + HTTP_PORT + "/api/docs");
        logger.info("OpenAPI Spec: http://localhost:" + HTTP_PORT + "/api/docs/openapi.json");
    }

    private void printEndpointSummary() {
        logger.info("========================================");
        logger.info("   REST API Endpoints Summary");
        logger.info("========================================");
        logger.info("");
        logger.info("Base URL: http://localhost:" + HTTP_PORT + "/api/v1");
        logger.info("");
        logger.info("Catalog API:");
        logger.info("  GET    /products           - Get all products");
        logger.info("  GET    /products/{id}      - Get product by ID");
        logger.info("  POST   /products           - Create product");
        logger.info("  PUT    /products/{id}      - Update product");
        logger.info("  DELETE /products/{id}      - Delete product");
        logger.info("  GET    /categories         - Get all categories");
        logger.info("");
        logger.info("Cart API:");
        logger.info("  POST   /cart/items         - Add to cart");
        logger.info("  GET    /cart?customerId={id} - View cart");
        logger.info("  PUT    /cart/items/{id}    - Update cart item");
        logger.info("  DELETE /cart/items/{id}    - Remove from cart");
        logger.info("");
        logger.info("Order API:");
        logger.info("  POST   /orders             - Create order");
        logger.info("  GET    /orders/{id}        - Get order by ID");
        logger.info("  GET    /orders/history     - Get order history");
        logger.info("  POST   /orders/{id}/payment - Process payment");
        logger.info("");
        logger.info("Customer API:");
        logger.info("  POST   /customers/register - Register customer");
        logger.info("  POST   /customers/login    - Login customer");
        logger.info("  GET    /customers/{id}     - Get customer profile");
        logger.info("  PUT    /customers/{id}     - Update profile");
        logger.info("");
        logger.info("Merchant API:");
        logger.info("  POST   /merchants/register  - Register merchant (FR-015)");
        logger.info("  POST   /merchants/login     - Login merchant (FR-015)");
        logger.info("  POST   /merchants/stores    - Create store");
        logger.info("  GET    /merchants/{id}/inventory - Get inventory");
        logger.info("  GET    /merchants/{id}/reports/sales - Get sales report");
        logger.info("  GET    /merchants/{id}/analytics/revenue - Get revenue analytics");
        logger.info("");
        logger.info("========================================");
        logger.info("API Documentation:");
        logger.info("  Swagger UI: http://localhost:8080/api/docs");
        logger.info("  OpenAPI Spec: http://localhost:8080/api/docs/openapi.json");
        logger.info("");
        logger.info("Test with: curl http://localhost:8080/api/v1/products");
        logger.info("========================================");
    }
}
