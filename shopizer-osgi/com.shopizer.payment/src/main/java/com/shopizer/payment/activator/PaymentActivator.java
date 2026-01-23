package com.shopizer.payment.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.shopizer.payment.api.PaymentProcessor;
import com.shopizer.payment.impl.PaymentProcessorImpl;

public class PaymentActivator implements BundleActivator {
    
    private ServiceRegistration<?> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println(">>> OSGi Payment Bundle: STARTING");
        
        // Register the service so the Order module can discover it
        registration = context.registerService(
            PaymentProcessor.class.getName(), 
            new PaymentProcessorImpl(), 
            null
        );
        
        System.out.println(">>> OSGi Payment Bundle: SERVICE REGISTERED SUCCESSFULLY");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println(">>> OSGi Payment Bundle: STOPPING");
        if (registration != null) {
            registration.unregister();
        }
    }
}