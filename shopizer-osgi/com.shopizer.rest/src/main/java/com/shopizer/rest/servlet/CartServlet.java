package com.shopizer.rest.servlet;

import com.shopizer.cart.api.CartService;
import com.shopizer.cart.dto.CartItemRequest;
import com.shopizer.cart.dto.CartResponse;
import com.shopizer.common.entity.Cart;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Cart Service
 *
 * Cart Management Endpoints:
 * GET    /api/v1/cart                           - Get all carts (Admin)
 * GET    /api/v1/cart?customerId={id}           - View cart by customer ID
 * GET    /api/v1/cart/{id}                      - Get cart by cart ID
 * GET    /api/v1/cart/customer/{customerId}     - Get cart by customer ID
 * POST   /api/v1/cart                           - Create new cart
 * DELETE /api/v1/cart/{id}                      - Delete cart by ID
 * DELETE /api/v1/cart?customerId={id}           - Clear cart by customer ID
 * DELETE /api/v1/cart/{cartId}/clear            - Clear cart items by cart ID
 *
 * Cart Item Endpoints (by cart ID):
 * POST   /api/v1/cart/{cartId}/items            - Add item to cart by cart ID
 * POST   /api/v1/cart/customer/{customerId}/items - Add item by customer ID
 * PUT    /api/v1/cart/{cartId}/items/{itemId}   - Update item by cart ID
 * DELETE /api/v1/cart/{cartId}/items/{itemId}   - Remove item by cart ID
 *
 * Cart Item Endpoints (by customer ID - legacy):
 * POST   /api/v1/cart/items                     - Add item to cart (customerId in body)
 * PUT    /api/v1/cart/items/{itemId}?customerId={id}&quantity={qty} - Update item quantity
 * DELETE /api/v1/cart/items/{itemId}?customerId={id} - Remove item from cart
 *
 * Cart Calculation Endpoints:
 * GET    /api/v1/cart/total?customerId={id}     - Calculate total by customer ID
 * GET    /api/v1/cart/{cartId}/total            - Get cart total by cart ID
 * GET    /api/v1/cart/count?customerId={id}     - Get item count
 *
 * Cart Validation & Merge:
 * POST   /api/v1/cart/validate?customerId={id}  - Validate cart
 * POST   /api/v1/cart/merge?anonymousCartId={id}&customerId={id} - Merge carts
 */
public class CartServlet extends BaseServlet {

    private CartService cartService;

    public CartServlet(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();
            Long customerId = getQueryParamAsLong(request, "customerId");

            if (pathInfo == null || pathInfo.equals("/")) {
                if (customerId != null) {
                    // GET /api/v1/cart?customerId={id} - View cart by customer ID
                    CartResponse cart = cartService.viewCart(customerId);
                    sendSuccess(response, cart);
                } else {
                    // GET /api/v1/cart - Get all carts (Admin)
                    List<Cart> carts = cartService.getAllCarts();
                    sendSuccess(response, carts);
                }
            } else if (pathInfo.equals("/total")) {
                // GET /api/v1/cart/total?customerId={id}
                if (customerId == null) {
                    sendBadRequest(response, "customerId is required");
                    return;
                }
                BigDecimal total = cartService.calculateTotal(customerId);
                sendSuccess(response, new TotalResponse(total));
            } else if (pathInfo.equals("/count")) {
                // GET /api/v1/cart/count?customerId={id}
                if (customerId == null) {
                    sendBadRequest(response, "customerId is required");
                    return;
                }
                Integer count = cartService.getCartItemCount(customerId);
                sendSuccess(response, new CountResponse(count));
            } else if (pathInfo.startsWith("/customer/")) {
                // GET /api/v1/cart/customer/{customerId}
                Long pathCustomerId = extractCustomerIdFromPath(pathInfo);
                if (pathCustomerId == null) {
                    sendBadRequest(response, "Invalid customer ID in path");
                    return;
                }
                CartResponse cart = cartService.getCartByCustomerId(pathCustomerId);
                sendSuccess(response, cart);
            } else if (pathInfo.matches("/\\d+/total")) {
                // GET /api/v1/cart/{cartId}/total
                Long cartId = extractIdFromPathSegment(pathInfo, 0);
                if (cartId == null) {
                    sendBadRequest(response, "Invalid cart ID");
                    return;
                }
                BigDecimal total = cartService.getCartTotalById(cartId);
                sendSuccess(response, new TotalResponse(total));
            } else if (pathInfo.matches("/\\d+")) {
                // GET /api/v1/cart/{id} - Get cart by ID
                Long cartId = Long.parseLong(pathInfo.substring(1));
                Cart cart = cartService.getCartById(cartId)
                    .orElse(null);
                if (cart == null) {
                    sendNotFound(response, "Cart not found with id: " + cartId);
                    return;
                }
                sendSuccess(response, cart);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in GET request", e);
            sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }

    // ========== GET Helper Methods ==========

    private Long extractCustomerIdFromPath(String pathInfo) {
        // Extract customer ID from /customer/{customerId} or /customer/{customerId}/items
        if (pathInfo.startsWith("/customer/")) {
            String remaining = pathInfo.substring("/customer/".length());
            int slashIndex = remaining.indexOf('/');
            String idStr = slashIndex > 0 ? remaining.substring(0, slashIndex) : remaining;
            try {
                return Long.parseLong(idStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Long extractIdFromPathSegment(String pathInfo, int segmentIndex) {
        // Extract ID from path segment (0-indexed after leading /)
        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length > segmentIndex) {
            try {
                return Long.parseLong(segments[segmentIndex]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // ========== POST Methods ==========

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/v1/cart - Create new cart
                Cart cart = readJsonBody(request, Cart.class);
                Cart createdCart = cartService.createCart(cart);
                sendCreated(response, createdCart);
            } else if (pathInfo.equals("/items")) {
                // POST /api/v1/cart/items (legacy - customer ID in body)
                AddToCartRequest addRequest = readJsonBody(request, AddToCartRequest.class);

                if (addRequest.getCustomerId() == null) {
                    sendBadRequest(response, "customerId is required");
                    return;
                }

                CartItemRequest itemRequest = new CartItemRequest();
                itemRequest.setProductId(addRequest.getProductId());
                itemRequest.setQuantity(addRequest.getQuantity());

                CartResponse cart = cartService.addToCart(addRequest.getCustomerId(), itemRequest);
                sendCreated(response, cart);
            } else if (pathInfo.matches("/\\d+/items")) {
                // POST /api/v1/cart/{cartId}/items - Add item by cart ID
                Long cartId = extractIdFromPathSegment(pathInfo, 0);
                if (cartId == null) {
                    sendBadRequest(response, "Invalid cart ID");
                    return;
                }

                CartItemRequest itemRequest = readJsonBody(request, CartItemRequest.class);
                CartResponse cart = cartService.addItemToCart(cartId, itemRequest);
                sendCreated(response, cart);
            } else if (pathInfo.startsWith("/customer/") && pathInfo.endsWith("/items")) {
                // POST /api/v1/cart/customer/{customerId}/items
                Long customerId = extractCustomerIdFromPath(pathInfo);
                if (customerId == null) {
                    sendBadRequest(response, "Invalid customer ID");
                    return;
                }

                CartItemRequest itemRequest = readJsonBody(request, CartItemRequest.class);
                CartResponse cart = cartService.addItemToCustomerCart(customerId, itemRequest);
                sendCreated(response, cart);
            } else if (pathInfo.equals("/validate")) {
                // POST /api/v1/cart/validate?customerId={id}
                Long customerId = getQueryParamAsLong(request, "customerId");

                if (customerId == null) {
                    sendBadRequest(response, "customerId is required");
                    return;
                }

                var validationResponse = cartService.validateCart(customerId);
                sendSuccess(response, validationResponse);
            } else if (pathInfo.equals("/merge")) {
                // POST /api/v1/cart/merge?anonymousCartId={id}&customerId={id}
                Long anonymousCartId = getQueryParamAsLong(request, "anonymousCartId");
                Long customerId = getQueryParamAsLong(request, "customerId");

                if (anonymousCartId == null || customerId == null) {
                    sendBadRequest(response, "Both anonymousCartId and customerId are required");
                    return;
                }

                CartResponse mergedCart = cartService.mergeCart(anonymousCartId, customerId);
                sendSuccess(response, mergedCart);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in POST request", e);
            sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }

    // ========== PUT Methods ==========

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.startsWith("/items/") && !pathInfo.contains("/")) {
                // PUT /api/v1/cart/items/{itemId}?customerId={id}&quantity={qty} (legacy)
                Long itemId = extractIdFromPath(request, "/items");
                Long customerId = getQueryParamAsLong(request, "customerId");
                Integer quantity = getQueryParamAsInt(request, "quantity");

                if (itemId == null || customerId == null || quantity == null) {
                    sendBadRequest(response, "itemId, customerId, and quantity are required");
                    return;
                }

                CartResponse cart = cartService.updateCartItem(customerId, itemId, quantity);
                sendSuccess(response, cart);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/items/\\d+")) {
                // PUT /api/v1/cart/{cartId}/items/{itemId}
                String[] segments = pathInfo.substring(1).split("/");
                if (segments.length != 3) {
                    sendBadRequest(response, "Invalid path");
                    return;
                }

                Long cartId = Long.parseLong(segments[0]);
                Long itemId = Long.parseLong(segments[2]);
                Integer quantity = getQueryParamAsInt(request, "quantity");

                if (quantity == null) {
                    sendBadRequest(response, "quantity parameter is required");
                    return;
                }

                CartResponse cart = cartService.updateCartItemById(cartId, itemId, quantity);
                sendSuccess(response, cart);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in PUT request", e);
            sendInternalError(response, "Error updating cart item: " + e.getMessage());
        }
    }

    // ========== DELETE Methods ==========

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();
            Long customerId = getQueryParamAsLong(request, "customerId");

            if (pathInfo == null || pathInfo.equals("/")) {
                if (customerId != null) {
                    // DELETE /api/v1/cart?customerId={id} - Clear cart by customer ID
                    cartService.clearCart(customerId);
                    sendNoContent(response);
                } else {
                    sendBadRequest(response, "customerId is required");
                }
            } else if (pathInfo.matches("/\\d+/clear")) {
                // DELETE /api/v1/cart/{cartId}/clear - Clear cart by cart ID
                Long cartId = extractIdFromPathSegment(pathInfo, 0);
                if (cartId == null) {
                    sendBadRequest(response, "Invalid cart ID");
                    return;
                }
                cartService.clearCartById(cartId);
                sendNoContent(response);
            } else if (pathInfo.matches("/\\d+")) {
                // DELETE /api/v1/cart/{id} - Delete cart by ID
                Long cartId = Long.parseLong(pathInfo.substring(1));
                cartService.deleteCartById(cartId);
                sendNoContent(response);
            } else if (pathInfo.matches("/\\d+/items/\\d+")) {
                // DELETE /api/v1/cart/{cartId}/items/{itemId}
                String[] segments = pathInfo.substring(1).split("/");
                if (segments.length != 3) {
                    sendBadRequest(response, "Invalid path");
                    return;
                }

                Long cartId = Long.parseLong(segments[0]);
                Long itemId = Long.parseLong(segments[2]);

                cartService.removeItemFromCartById(cartId, itemId);
                sendNoContent(response);
            } else if (pathInfo.startsWith("/items/")) {
                // DELETE /api/v1/cart/items/{itemId}?customerId={id} (legacy)
                Long itemId = extractIdFromPath(request, "/items");

                if (customerId == null) {
                    sendBadRequest(response, "customerId is required");
                    return;
                }

                if (itemId == null) {
                    sendBadRequest(response, "itemId is required");
                    return;
                }

                cartService.removeFromCart(customerId, itemId);
                sendNoContent(response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in DELETE request", e);
            sendInternalError(response, "Error deleting cart item: " + e.getMessage());
        }
    }

    // Helper DTOs
    static class AddToCartRequest {
        private Long customerId;
        private Long productId;
        private Integer quantity;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    static class TotalResponse {
        private BigDecimal total;

        public TotalResponse(BigDecimal total) { this.total = total; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
    }

    static class CountResponse {
        private Integer count;

        public CountResponse(Integer count) { this.count = count; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}
