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
 * POST   /api/v1/customers/register              - Register customer
 * POST   /api/v1/customers/login                 - Login customer
 * POST   /api/v1/customers/validate-token        - Validate token
 * GET    /api/v1/customers/{id}                  - Get customer by ID
 * GET    /api/v1/customers/email?email={email}   - Get customer by email
 * PUT    /api/v1/customers/{id}                  - Update profile
 * POST   /api/v1/customers/{id}/change-password  - Change password
 * POST   /api/v1/customers/{id}/addresses        - Add address
 * GET    /api/v1/customers/{id}/addresses        - Get addresses
 * PUT    /api/v1/customers/{id}/addresses/{addressId} - Update address
 * DELETE /api/v1/customers/{id}/addresses/{addressId} - Delete address
 * PUT    /api/v1/customers/{id}/addresses/{addressId}/default - Set default address
 * POST   /api/v1/customers/{id}/deactivate       - Deactivate account
 * POST   /api/v1/customers/{id}/activate         - Activate account
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
                sendBadRequest(response, "Customer ID or email is required");
                return;
            }

            if (pathInfo.equals("/email")) {
                // GET /api/v1/customers/email?email={email}
                handleGetByEmail(request, response);
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
                sendBadRequest(response, "Invalid endpoint");
                return;
            }

            if (pathInfo.equals("/register")) {
                // POST /api/v1/customers/register
                CustomerRegistrationRequest regRequest = readJsonBody(request, CustomerRegistrationRequest.class);
                CustomerAuthResponse authResponse = customerService.register(regRequest);
                sendCreated(response, authResponse);
            } else if (pathInfo.equals("/login")) {
                // POST /api/v1/customers/login
                CustomerLoginRequest loginRequest = readJsonBody(request, CustomerLoginRequest.class);
                CustomerAuthResponse authResponse = customerService.login(loginRequest);
                sendSuccess(response, authResponse);
            } else if (pathInfo.equals("/validate-token")) {
                // POST /api/v1/customers/validate-token
                TokenRequest tokenRequest = readJsonBody(request, TokenRequest.class);
                CustomerResponse customer = customerService.validateToken(tokenRequest.getToken());
                if (customer != null) {
                    sendSuccess(response, customer);
                } else {
                    sendError(response, "Invalid token", HttpServletResponse.SC_UNAUTHORIZED);
                }
            } else if (pathInfo.contains("/change-password")) {
                // POST /api/v1/customers/{id}/change-password
                handleChangePassword(request, response);
            } else if (pathInfo.contains("/addresses") && !pathInfo.contains("/default")) {
                // POST /api/v1/customers/{id}/addresses
                handleAddAddress(request, response);
            } else if (pathInfo.contains("/deactivate")) {
                // POST /api/v1/customers/{id}/deactivate
                handleDeactivate(request, response);
            } else if (pathInfo.contains("/activate")) {
                // POST /api/v1/customers/{id}/activate
                handleActivate(request, response);
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

            if (pathInfo.contains("/addresses") && pathInfo.contains("/default")) {
                // PUT /api/v1/customers/{id}/addresses/{addressId}/default
                handleSetDefaultAddress(request, response);
            } else if (pathInfo.contains("/addresses")) {
                // PUT /api/v1/customers/{id}/addresses/{addressId}
                handleUpdateAddress(request, response);
            } else {
                // PUT /api/v1/customers/{id}
                handleUpdateProfile(request, response);
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
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in DELETE request", e);
            sendInternalError(response, "Error deleting address: " + e.getMessage());
        }
    }

    private void handleGetById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = extractIdFromPath(request, "/api/v1/customers");

        if (customerId == null) {
            sendBadRequest(response, "Invalid customer ID");
            return;
        }

        CustomerResponse customer = customerService.getCustomerById(customerId);
        if (customer != null) {
            sendSuccess(response, customer);
        } else {
            sendNotFound(response, "Customer not found with ID: " + customerId);
        }
    }

    private void handleGetByEmail(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");

        if (email == null || email.isEmpty()) {
            sendBadRequest(response, "Email is required");
            return;
        }

        CustomerResponse customer = customerService.getCustomerByEmail(email);
        if (customer != null) {
            sendSuccess(response, customer);
        } else {
            sendNotFound(response, "Customer not found with email: " + email);
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = extractIdFromPath(request, "/api/v1/customers");

        if (customerId == null) {
            sendBadRequest(response, "Invalid customer ID");
            return;
        }

        CustomerUpdateRequest updateRequest = readJsonBody(request, CustomerUpdateRequest.class);
        CustomerResponse updated = customerService.updateProfile(customerId, updateRequest);
        sendSuccess(response, updated);
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

    private void handleDeactivate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Customer ID is required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            customerService.deactivateAccount(customerId);
            sendSuccess(response, new MessageResponse("Account deactivated successfully"));
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID");
        }
    }

    private void handleActivate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Customer ID is required");
            return;
        }

        try {
            Long customerId = Long.parseLong(parts[1]);
            customerService.activateAccount(customerId);
            sendSuccess(response, new MessageResponse("Account activated successfully"));
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid customer ID");
        }
    }

    // Helper DTOs
    static class TokenRequest {
        private String token;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    static class MessageResponse {
        private String message;

        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
