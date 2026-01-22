package com.shopizer.order.activator;

import com.shopizer.cart.api.CartService;
import com.shopizer.catalog.api.CatalogService;
import com.shopizer.order.api.OrderService;
import com.shopizer.order.impl.OrderServiceImpl;
import com.shopizer.order.payment.PayPalPaymentProcessor;
import com.shopizer.order.payment.PaymentProcessor;
import com.shopizer.order.payment.StripePaymentProcessor;
import com.shopizer.order.repository.OrderItemRepository;
import com.shopizer.order.repository.OrderRepository;
import com.shopizer.order.repository.PaymentRepository;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * OSGI Bundle Activator for Order Module
 * Manages lifecycle and service registration
 * Depends on CartService and CatalogService
 */
public class OrderActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(OrderActivator.class);

    private ServiceRegistration<OrderService> orderServiceRegistration;
    private List<ServiceRegistration<PaymentProcessor>> paymentProcessorRegistrations = new ArrayList<>();

    private ServiceTracker<CartService, CartService> cartServiceTracker;
    private ServiceTracker<CatalogService, CatalogService> catalogServiceTracker;

    private CartService cartService;
    private CatalogService catalogService;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Order Module...");

        // Register payment processors first (as they are dependencies)
        registerPaymentProcessors(context);

        // Track CartService (dependency)
        cartServiceTracker = new ServiceTracker<CartService, CartService>(
            context,
            CartService.class,
            null
        ) {
            @Override
            public CartService addingService(ServiceReference<CartService> reference) {
                CartService service = super.addingService(reference);
                logger.info("CartService detected");
                cartService = service;
                tryRegisterOrderService(context);
                return service;
            }

            @Override
            public void removedService(ServiceReference<CartService> reference, CartService service) {
                logger.info("CartService removed, unregistering OrderService...");
                cartService = null;
                unregisterOrderService();
                super.removedService(reference, service);
            }
        };

        // Track CatalogService (dependency)
        catalogServiceTracker = new ServiceTracker<CatalogService, CatalogService>(
            context,
            CatalogService.class,
            null
        ) {
            @Override
            public CatalogService addingService(ServiceReference<CatalogService> reference) {
                CatalogService service = super.addingService(reference);
                logger.info("CatalogService detected");
                catalogService = service;
                tryRegisterOrderService(context);
                return service;
            }

            @Override
            public void removedService(ServiceReference<CatalogService> reference, CatalogService service) {
                logger.info("CatalogService removed, unregistering OrderService...");
                catalogService = null;
                unregisterOrderService();
                super.removedService(reference, service);
            }
        };

        cartServiceTracker.open();
        catalogServiceTracker.open();

        logger.info("Order Module started. Waiting for CartService and CatalogService...");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Order Module...");

        if (cartServiceTracker != null) {
            cartServiceTracker.close();
        }

        if (catalogServiceTracker != null) {
            catalogServiceTracker.close();
        }

        unregisterOrderService();
        unregisterPaymentProcessors();

        logger.info("Order Module stopped successfully.");
    }

    /**
     * Register payment processor components
     */
    private void registerPaymentProcessors(BundleContext context) {
        logger.info("Registering payment processors...");

        // Register Stripe processor
        PaymentProcessor stripeProcessor = new StripePaymentProcessor();
        Dictionary<String, Object> stripeProps = new Hashtable<>();
        stripeProps.put("service.description", "Stripe Payment Processor");
        stripeProps.put("service.vendor", "Shopizer");
        stripeProps.put("payment.method", "Stripe");

        ServiceRegistration<PaymentProcessor> stripeReg = context.registerService(
            PaymentProcessor.class,
            stripeProcessor,
            stripeProps
        );
        paymentProcessorRegistrations.add(stripeReg);

        // Register PayPal processor
        PaymentProcessor paypalProcessor = new PayPalPaymentProcessor();
        Dictionary<String, Object> paypalProps = new Hashtable<>();
        paypalProps.put("service.description", "PayPal Payment Processor");
        paypalProps.put("service.vendor", "Shopizer");
        paypalProps.put("payment.method", "PayPal");

        ServiceRegistration<PaymentProcessor> paypalReg = context.registerService(
            PaymentProcessor.class,
            paypalProcessor,
            paypalProps
        );
        paymentProcessorRegistrations.add(paypalReg);

        logger.info("Payment processors registered: Stripe, PayPal");
    }

    /**
     * Unregister payment processors
     */
    private void unregisterPaymentProcessors() {
        for (ServiceRegistration<PaymentProcessor> registration : paymentProcessorRegistrations) {
            try {
                registration.unregister();
            } catch (IllegalStateException e) {
                logger.warn("Payment processor already unregistered");
            }
        }
        paymentProcessorRegistrations.clear();
        logger.info("Payment processors unregistered");
    }

    /**
     * Try to register OrderService if all dependencies are available
     */
    private synchronized void tryRegisterOrderService(BundleContext context) {
        if (orderServiceRegistration != null) {
            logger.debug("OrderService already registered");
            return;
        }

        if (cartService == null || catalogService == null) {
            logger.debug("Not all dependencies available yet. CartService: {}, CatalogService: {}",
                cartService != null, catalogService != null);
            return;
        }

        logger.info("All dependencies available, registering OrderService...");
        registerOrderService(context);
    }

    /**
     * Register OrderService in OSGI registry
     */
    private void registerOrderService(BundleContext context) {
        try {
            // For now, we'll create placeholder implementations
            OrderRepository orderRepository = null; // Will be injected by OSGI framework
            OrderItemRepository orderItemRepository = null; // Will be injected by OSGI framework
            PaymentRepository paymentRepository = null; // Will be injected by OSGI framework

            // Create payment processor list
            List<PaymentProcessor> paymentProcessors = new ArrayList<>();
            paymentProcessors.add(new StripePaymentProcessor());
            paymentProcessors.add(new PayPalPaymentProcessor());

            // Create service implementation
            OrderService orderService = new OrderServiceImpl(
                orderRepository,
                orderItemRepository,
                paymentRepository,
                cartService,
                catalogService,
                paymentProcessors
            );

            // Register service in OSGI registry
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("service.description", "Order Service for Order Management and Payment Processing");
            properties.put("service.vendor", "Shopizer");
            properties.put("service.version", "1.0.0");

            orderServiceRegistration = context.registerService(
                OrderService.class,
                orderService,
                properties
            );

            logger.info("OrderService registered successfully.");
        } catch (Exception e) {
            logger.error("Failed to register OrderService", e);
        }
    }

    /**
     * Unregister OrderService from OSGI registry
     */
    private synchronized void unregisterOrderService() {
        if (orderServiceRegistration != null) {
            try {
                orderServiceRegistration.unregister();
                orderServiceRegistration = null;
                logger.info("OrderService unregistered.");
            } catch (IllegalStateException e) {
                logger.warn("OrderService already unregistered.");
            }
        }
    }
}
