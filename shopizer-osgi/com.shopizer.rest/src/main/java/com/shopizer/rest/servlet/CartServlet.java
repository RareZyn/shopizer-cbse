package com.shopizer.rest.servlet;

import com.shopizer.cart.api.CartService;
import com.shopizer.cart.dto.CartItemRequest;
import com.shopizer.cart.dto.CartResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * REST Controller for Cart Service
 *
 * Endpoints:
 * POST   /api/v1/cart/items                     - Add item to cart
 * GET    /api/v1/cart?customerId={id}           - View cart
 * PUT    /api/v1/cart/items/{itemId}?customerId={id}&quantity={qty} - Update item quantity
 * DELETE /api/v1/cart/items/{itemId}?customerId={id} - Remove item from cart
 * DELETE /api/v1/cart?customerId={id}           - Clear cart
 * GET    /api/v1/cart/total?customerId={id}     - Calculate total
 * GET    /api/v1/cart/count?customerId={id}     - Get item count
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

            if (customerId == null) {
                sendBadRequest(response, "customerId is required");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/cart?customerId={id}
                CartResponse cart = cartService.viewCart(customerId);
                sendSuccess(response, cart);
            } else if (pathInfo.equals("/total")) {
                // GET /api/v1/cart/total?customerId={id}
                BigDecimal total = cartService.calculateTotal(customerId);
                sendSuccess(response, new TotalResponse(total));
            } else if (pathInfo.equals("/count")) {
                // GET /api/v1/cart/count?customerId={id}
                Integer count = cartService.getCartItemCount(customerId);
                sendSuccess(response, new CountResponse(count));
            } else {
                sendBadRequest(response, "Invalid endpoint");
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

            if (pathInfo != null && pathInfo.equals("/items")) {
                // POST /api/v1/cart/items
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
            } else if (pathInfo != null && pathInfo.equals("/validate")) {
                // POST /api/v1/cart/validate?customerId={id}
                Long customerId = getQueryParamAsLong(request, "customerId");

                if (customerId == null) {
                    sendBadRequest(response, "customerId is required");
                    return;
                }

                var validationResponse = cartService.validateCart(customerId);
                sendSuccess(response, validationResponse);
            } else if (pathInfo != null && pathInfo.equals("/merge")) {
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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                // PUT /api/v1/cart/items/{itemId}?customerId={id}&quantity={qty}
                Long itemId = extractIdFromPath(request, "/items");
                Long customerId = getQueryParamAsLong(request, "customerId");
                Integer quantity = getQueryParamAsInt(request, "quantity");

                if (itemId == null || customerId == null || quantity == null) {
                    sendBadRequest(response, "itemId, customerId, and quantity are required");
                    return;
                }

                CartResponse cart = cartService.updateCartItem(customerId, itemId, quantity);
                sendSuccess(response, cart);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in PUT request", e);
            sendInternalError(response, "Error updating cart item: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();
            Long customerId = getQueryParamAsLong(request, "customerId");

            if (customerId == null) {
                sendBadRequest(response, "customerId is required");
                return;
            }

            if (pathInfo != null && pathInfo.startsWith("/items/")) {
                // DELETE /api/v1/cart/items/{itemId}?customerId={id}
                Long itemId = extractIdFromPath(request, "/items");

                if (itemId == null) {
                    sendBadRequest(response, "itemId is required");
                    return;
                }

                cartService.removeFromCart(customerId, itemId);
                sendNoContent(response);
            } else if (pathInfo == null || pathInfo.equals("/")) {
                // DELETE /api/v1/cart?customerId={id}
                cartService.clearCart(customerId);
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
