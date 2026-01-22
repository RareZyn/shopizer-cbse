package com.shopizer.rest.servlet;

import com.shopizer.customer.api.CustomerService;
import com.shopizer.customer.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * REST Controller for Customer Service
 *
 * Endpoints:
 * GET    /api/v1/customers                      - Get all customers (Admin)
 * POST   /api/v1/customers                      - Create customer (Admin)
 * GET    /api/v1/customers/{id}                 - Get customer by ID
 * PUT    /api/v1/customers/{id}                 - Update customer (Admin)
 * DELETE /api/v1/customers/{id}                 - Delete customer (Admin)
 * POST   /api/v1/customers/register             - Register customer (FR-024)
 * POST   /api/v1/customers/login                - Login customer (FR-025)
 * POST   /api/v1/customers/{customerId}/logout  - Logout customer (FR-025)
 * GET    /api/v1/customers/email/{email}        - Get customer by email
 * GET    /api/v1/customers/{customerId}/profile - Get customer profile (FR-026)
 * PUT    /api/v1/customers/{customerId}/profile - Update customer profile (FR-026)
 * POST   /api/v1/customers/{id}/change-password - Change password
 * POST   /api/v1/customers/{id}/addresses       - Add address
 * GET    /api/v1/customers/{id}/addresses       - Get addresses
 * PUT    /api/v1/customers/{id}/addresses/{addressId} - Update address
 * DELETE /api/v1/customers/{id}/addresses/{addressId} - Delete address
 * PUT    /api/v1/customers/{id}/addresses/{addressId}/default - Set default address
 */
public class CustomerServlet extends BaseServlet {

    private CustomerService customerService;

    public CustomerServlet(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/customers - Get all customers (Admin)
                List<CustomerResponse> customers = customerService.getAllCustomers();
                sendSuccess(response, customers);
                return;
            }

            if (pathInfo.startsWith("/email/")) {
                // GET /api/v1/customers/email/{email}
                String email = pathInfo.substring("/email/".length());
                CustomerResponse customer = customerService.getCustomerByEmail(email);
                sendSuccess(response, customer);
            } else if (pathInfo.contains("/profile")) {
                // GET /api/v1/customers/{customerId}/profile
                handleGetProfile(request, response);
            } else if (pathInfo.contains("/addresses")) {
                // GET /api/v1/customers/{id}/addresses
                handleGetAddresses(request, response);
            } else {
                // GET /api/v1/customers/{id}
                handleGetById(request, response);
            }
        } catch (Exception e) {
            logger.error("Error in GET request", e);
            sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/v1/customers - Create customer (Admin)
                CustomerRegistrationRequest regRequest = readJsonBody(request, CustomerRegistrationRequest.class);
                CustomerResponse customer = customerService.createCustomer(regRequest);
                sendCreated(response, customer);
            } else if (pathInfo.equals("/register")) {
                // POST /api/v1/customers/register
                CustomerRegistrationRequest regRequest = readJsonBody(request, CustomerRegistrationRequest.class);
                CustomerResponse customer = customerService.register(regRequest);
                sendCreated(response, customer);
            } else if (pathInfo.equals("/login")) {
                // POST /api/v1/customers/login
                CustomerLoginRequest loginRequest = readJsonBody(request, CustomerLoginRequest.class);
                CustomerLoginResponse loginResponse = customerService.login(loginRequest);
                sendSuccess(response, loginResponse);
            } else if (pathInfo.contains("/logout")) {
                // POST /api/v1/customers/{customerId}/logout
                handleLogout(request, response);
            } else if (pathInfo.contains("/change-password")) {
                // POST /api/v1/customers/{id}/change-password
                handleChangePassword(request, response);
            } else if (pathInfo.contains("/addresses") && !pathInfo.contains("/default")) {
                // POST /api/v1/customers/{id}/addresses
                handleAddAddress(request, response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in POST request", e);
            sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequest(response, "Invalid endpoint");
                return;
            }

            if (pathInfo.contains("/profile")) {
                // PUT /api/v1/customers/{customerId}/profile
                handleUpdateProfile(request, response);
            } else if (pathInfo.contains("/addresses") && pathInfo.contains("/default")) {
                // PUT /api/v1/customers/{id}/addresses/{addressId}/default
                handleSetDefaultAddress(request, response);
            } else if (pathInfo.contains("/addresses")) {
                // PUT /api/v1/customers/{id}/addresses/{addressId}
                handleUpdateAddress(request, response);
            } else {
                // PUT /api/v1/customers/{id} - Update customer (Admin)
                handleUpdateCustomer(request, response);
            }
        } catch (Exception e) {
            logger.error("Error in PUT request", e);
            sendInternalError(response, "Error updating customer: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.contains("/addresses")) {
                // DELETE /api/v1/customers/{id}/addresses/{addressId}
                handleDeleteAddress(request, response);
            } else if (pathInfo != null && !pathInfo.equals("/")) {
                // DELETE /api/v1/customers/{id} - Delete customer (Admin)
                handleDeleteCustomer(request, response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in DELETE request", e);
            sendInternalError(response, "Error deleting address: " + e.getMessage());
        }
    }

    // ========== GET Helper Methods ==========

    private void handleGetById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = extractIdFromPath(request, "/api/v1/customers");

        if (customerId == null) {
            sendBadRequest(response, "Invalid customer ID");
            return;
        }

        CustomerResponse customer = customerService.getCustomerById(customerId);
        sendSuccess(response, customer);
    }

    private void handleGetProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Customer ID is required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            CustomerResponse customer = customerService.getCustomerProfile(customerId);
            sendSuccess(response, customer);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID");
        }
    }

    private void handleGetAddresses(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Customer ID is required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            List<AddressResponse> addresses = customerService.getAddresses(customerId);
            sendSuccess(response, addresses);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID");
        }
    }

    // ========== POST Helper Methods ==========

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Customer ID is required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            customerService.logout(customerId);
            sendSuccess(response, new MessageResponse("Logout successful"));
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID");
        } catch (Exception e) {
            logger.error("Error during logout", e);
            sendInternalError(response, "Error processing logout: " + e.getMessage());
        }
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Customer ID is required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            PasswordChangeRequest passwordRequest = readJsonBody(request, PasswordChangeRequest.class);
            customerService.changePassword(customerId, passwordRequest);
            sendSuccess(response, new MessageResponse("Password changed successfully"));
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID");
        }
    }

    private void handleAddAddress(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Customer ID is required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            AddressRequest addressRequest = readJsonBody(request, AddressRequest.class);
            AddressResponse address = customerService.addAddress(customerId, addressRequest);
            sendCreated(response, address);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID");
        }
    }

    // ========== PUT Helper Methods ==========

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Customer ID is required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            CustomerProfileUpdateRequest updateRequest = readJsonBody(request, CustomerProfileUpdateRequest.class);
            CustomerResponse updated = customerService.updateProfile(customerId, updateRequest);
            sendSuccess(response, updated);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID");
        }
    }

    private void handleUpdateCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = extractIdFromPath(request, "/api/v1/customers");

        if (customerId == null) {
            sendBadRequest(response, "Invalid customer ID");
            return;
        }

        CustomerUpdateRequest updateRequest = readJsonBody(request, CustomerUpdateRequest.class);
        CustomerResponse updated = customerService.updateCustomer(customerId, updateRequest);
        sendSuccess(response, updated);
    }

    private void handleUpdateAddress(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 4) {
            sendBadRequest(response, "Customer ID and Address ID are required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            Long addressId = Long.parseLong(parts[3]);
            AddressRequest addressRequest = readJsonBody(request, AddressRequest.class);
            AddressResponse updated = customerService.updateAddress(customerId, addressId, addressRequest);
            sendSuccess(response, updated);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID or address ID");
        }
    }

    private void handleSetDefaultAddress(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 4) {
            sendBadRequest(response, "Customer ID and Address ID are required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            Long addressId = Long.parseLong(parts[3]);
            customerService.setDefaultAddress(customerId, addressId);
            sendSuccess(response, new MessageResponse("Default address set successfully"));
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID or address ID");
        }
    }

    // ========== DELETE Helper Methods ==========

    private void handleDeleteCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = extractIdFromPath(request, "/api/v1/customers");

        if (customerId == null) {
            sendBadRequest(response, "Invalid customer ID");
            return;
        }

        customerService.deleteCustomer(customerId);
        sendNoContent(response);
    }

    private void handleDeleteAddress(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 4) {
            sendBadRequest(response, "Customer ID and Address ID are required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            Long addressId = Long.parseLong(parts[3]);
            customerService.deleteAddress(customerId, addressId);
            sendNoContent(response);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID or address ID");
        }
    }

    // Helper DTOs
    static class TokenRequest {
        private String token;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    static class LogoutRequest {
        private Long customerId;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
    }

    static class MessageResponse {
        private String message;

        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
