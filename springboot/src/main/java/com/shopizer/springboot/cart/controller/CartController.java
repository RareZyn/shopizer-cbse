package com.shopizer.springboot.cart.controller;

import com.shopizer.springboot.cart.dto.CartItemRequest;
import com.shopizer.springboot.cart.dto.CartItemRequestWithCustomer;
import com.shopizer.springboot.cart.dto.CartRequest;
import com.shopizer.springboot.cart.dto.CartResponse;
import com.shopizer.springboot.cart.dto.CartValidationResponse;
import com.shopizer.springboot.cart.entity.Cart;
import com.shopizer.springboot.cart.entity.CartItem;
import com.shopizer.springboot.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Cart REST Controller
 * RESTful API endpoints for shopping cart and cart item operations.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: Cart item management (Add, View, Update, Remove)
 * - FR-007: Calculate and display cart total automatically
 * - FR-008: Validate product availability before adding to cart
 * - FR-009: Persist cart data for logged-in customers
 *
 * USE CASES IMPLEMENTED:
 *
 * UC04: Add to Cart
 * Endpoints:
 *   - POST /api/v1/cart/{cartId}/items - Add item to cart by cart ID
 *   - POST /api/v1/cart/customer/{customerId}/items - Add item by customer ID
 * Request Body: CartItemRequest (productId, quantity)
 * Response: 201 Created with CartItem entity
 * Validation: FR-008 (product exists, active, stock available)
 *
 * UC05: View Cart
 * Endpoints:
 *   - GET /api/v1/cart/{cartId} - Get cart by cart ID
 *   - GET /api/v1/cart/customer/{customerId} - Get cart by customer ID
 * Response Body: CartResponse DTO with items[], total, itemCount
 * Empty Cart: Returns items=[], total=0.00, itemCount=0
 *
 * UC06: Update Cart
 * Endpoints:
 *   - PUT /api/v1/cart/{cartId}/items/{itemId} - Update item quantity
 *   - DELETE /api/v1/cart/{cartId}/items/{itemId} - Remove single item
 *   - DELETE /api/v1/cart/{cartId}/clear - Clear all items from cart
 * Update Request: {"quantity": Integer}
 * Update Response: 200 OK with updated CartItem
 * Remove Response: 204 No Content (successful deletion)
 * Re-validation: FR-008 (stock check on quantity increase)
 *
 * ADDITIONAL ENDPOINTS:
 * - POST /api/v1/cart - Create new cart (FR-009)
 * - GET /api/v1/cart - List all carts (Admin function)
 * - DELETE /api/v1/cart/{cartId} - Delete entire cart
 * - GET /api/v1/cart/{cartId}/total - Get cart total only (FR-007)
 * - POST /api/v1/cart/merge - Merge guest cart to customer cart
 *
 * ROLE IN ARCHITECTURE:
 * - CONTROLLER LAYER (Presentation/REST API)
 * - Handles HTTP requests and responses
 * - Validates request input via @Valid annotation (CartItemRequest)
 * - Delegates business logic to CartService
 * - Returns appropriate HTTP status codes:
 *   * 200 OK - Successful retrieval/update
 *   * 201 Created - Successful creation
 *   * 204 No Content - Successful deletion
 *   * 404 Not Found - Resource not found
 *   * 409 Conflict - Insufficient stock
 *   * 500 Internal Server Error - Server errors
 *
 * API DOCUMENTATION:
 * - Full Swagger/OpenAPI annotations on all endpoints
 * - @Tag: Module-level documentation
 * - @Operation: Endpoint description with FR/UC references
 * - @ApiResponses: HTTP status codes and meanings
 * - @Parameter: Path/query parameter descriptions
 * - Accessible at: http://localhost:8080/swagger-ui.html
 *
 * REQUEST/RESPONSE FLOW:
 * 1. Client sends HTTP request
 * 2. Spring validates @Valid request body (CartItemRequest)
 * 3. Controller extracts path variables and request body
 * 4. Controller calls appropriate CartService method
 * 5. Service executes business logic (validation, persistence)
 * 6. Controller wraps response in ResponseEntity with HTTP status
 * 7. Spring serializes response to JSON
 * 8. HTTP response sent to client
 *
 * ERROR HANDLING:
 * - Exceptions thrown by Service layer
 * - Caught by global exception handler (@ControllerAdvice)
 * - Converted to appropriate HTTP error responses
 * - ResourceNotFoundException → 404 Not Found
 * - BadRequestException → 400 Bad Request
 * - InsufficientStockException → 409 Conflict
 *
 * TOTAL ENDPOINTS: 12 RESTful endpoints
 * TOTAL IMPLEMENTATION: 288 lines of code
 */
@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping Cart APIs for managing cart items and checkout (FR-006 to FR-009)")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ========== Cart Management Endpoints ==========

    /**
     * UC05: View Cart - List all carts or get cart by customer ID (query param)
     */
    @GetMapping
    @Operation(
        summary = "Get all carts or cart by customer ID",
        description = "FR-006: Retrieve all carts (Admin function) or get cart by customerId query parameter. Returns list of all carts or single cart."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved carts"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCarts(
        @Parameter(description = "Customer ID (optional)", required = false)
        @RequestParam(required = false) Long customerId
    ) {
        if (customerId != null) {
            CartResponse response = cartService.getCartResponseByCustomerId(customerId);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(cartService.getAllCarts());
    }

    /**
     * UC05: View Cart - Get cart by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get cart by ID",
        description = "UC05: View Cart. FR-006: Retrieve cart details by cart ID with all items and calculated total (FR-007)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart found and returned"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CartResponse> getCartById(
        @Parameter(description = "Cart ID", required = true)
        @PathVariable Long id
    ) {
        CartResponse response = cartService.getCartResponse(id);
        return ResponseEntity.ok(response);
    }

    /**
     * UC05: View Cart - Get cart by customer ID
     */
    @GetMapping("/customer/{customerId}")
    @Operation(
        summary = "Get cart by customer ID",
        description = "UC05: View Cart. FR-006, FR-009: Retrieve customer's cart with all items. Creates new cart if none exists."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart found or created and returned"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CartResponse> getCartByCustomerId(
        @Parameter(description = "Customer ID", required = true)
        @PathVariable Long customerId
    ) {
        CartResponse response = cartService.getCartResponseByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * FR-009: Create new cart
     */
    @PostMapping
    @Operation(
        summary = "Create a new cart",
        description = "FR-009: Create a new cart for a customer or session. Used for explicit cart creation."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cart created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Cart> createCart(
        @Parameter(description = "Cart creation request", required = true)
        @RequestBody CartRequest request
    ) {
        Cart cart = new Cart();
        cart.setCustomerId(request.getCustomerId());
        cart.setSessionId(request.getSessionId());
        cart.setStatus("ACTIVE");
        Cart created = cartService.createCart(cart);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * FR-006: Delete cart
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete cart",
        description = "FR-006: Delete entire cart including all items. Use with caution."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cart deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteCart(
        @Parameter(description = "Cart ID", required = true)
        @PathVariable Long id
    ) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * UC06: Update Cart - Clear all items from cart or delete cart by customer ID
     */
    @DeleteMapping("/{cartId}/clear")
    @Operation(
        summary = "Clear cart items",
        description = "UC06: Update Cart. FR-006: Remove all items from cart but keep cart entity."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cart cleared successfully"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> clearCart(
        @Parameter(description = "Cart ID", required = true)
        @PathVariable Long cartId
    ) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    /**
     * UC06: Clear cart by customer ID (query parameter variant)
     */
    @DeleteMapping(params = "customerId")
    @Operation(
        summary = "Clear cart by customer ID",
        description = "UC06: Update Cart. FR-006: Clear all items from customer's cart using query parameter."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cart cleared successfully"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> clearCartByCustomerId(
        @Parameter(description = "Customer ID", required = true)
        @RequestParam Long customerId
    ) {
        Cart cart = cartService.getOrCreateCartByCustomerId(customerId);
        cartService.clearCart(cart.getId());
        return ResponseEntity.noContent().build();
    }

    // ========== Cart Item Management Endpoints ==========

    /**
     * UC04: Add to Cart - Add item by cart ID
     */
    @PostMapping("/{cartId}/items")
    @Operation(
        summary = "Add item to cart",
        description = "UC04: Add to Cart. FR-006: Add product to cart. FR-008: Validates product availability and stock before adding."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item added successfully"),
        @ApiResponse(responseCode = "404", description = "Cart or product not found"),
        @ApiResponse(responseCode = "400", description = "Product not active"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CartItem> addItemToCart(
        @Parameter(description = "Cart ID", required = true)
        @PathVariable Long cartId,
        @Parameter(description = "Cart item request with productId and quantity", required = true)
        @Valid @RequestBody CartItemRequest request
    ) {
        CartItem item = cartService.addItemToCart(cartId, request);
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    /**
     * UC04: Add to Cart - Add item by customer ID
     */
    @PostMapping("/customer/{customerId}/items")
    @Operation(
        summary = "Add item to customer cart",
        description = "UC04: Add to Cart. FR-006: Add product to customer's cart. Auto-creates cart if none exists. FR-008: Validates product availability."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item added successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Product not active"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CartItem> addItemToCustomerCart(
        @Parameter(description = "Customer ID", required = true)
        @PathVariable Long customerId,
        @Parameter(description = "Cart item request with productId and quantity", required = true)
        @Valid @RequestBody CartItemRequest request
    ) {
        CartItem item = cartService.addItemToCustomerCart(customerId, request);
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    /**
     * UC06: Update Cart - Update item quantity
     */
    @PutMapping("/{cartId}/items/{itemId}")
    @Operation(
        summary = "Update cart item quantity",
        description = "UC06: Update Cart. FR-006: Update item quantity. FR-008: Re-validates stock availability for new quantity."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item updated successfully"),
        @ApiResponse(responseCode = "404", description = "Cart or item not found"),
        @ApiResponse(responseCode = "400", description = "Invalid quantity or product not active"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CartItem> updateCartItemQuantity(
        @Parameter(description = "Cart ID", required = true)
        @PathVariable Long cartId,
        @Parameter(description = "Cart Item ID", required = true)
        @PathVariable Long itemId,
        @Parameter(description = "New quantity (must be >= 1)", required = true)
        @RequestParam Integer quantity
    ) {
        CartItem item = cartService.updateCartItemQuantity(cartId, itemId, quantity);
        return ResponseEntity.ok(item);
    }

    /**
     * UC06: Update Cart - Remove item from cart
     */
    @DeleteMapping("/{cartId}/items/{itemId}")
    @Operation(
        summary = "Remove item from cart",
        description = "UC06: Update Cart. FR-006: Remove specific item from cart."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Item removed successfully"),
        @ApiResponse(responseCode = "404", description = "Cart or item not found"),
        @ApiResponse(responseCode = "400", description = "Item does not belong to cart"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> removeItemFromCart(
        @Parameter(description = "Cart ID", required = true)
        @PathVariable Long cartId,
        @Parameter(description = "Cart Item ID", required = true)
        @PathVariable Long itemId
    ) {
        cartService.removeItemFromCart(cartId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * UC04: Add to Cart - Add item (customer ID in body - legacy OSGi compatibility)
     */
    @PostMapping("/items")
    @Operation(
        summary = "Add item to cart (legacy)",
        description = "UC04: Add to Cart. FR-006: Add product to customer's cart. OSGi compatibility endpoint with customerId in request body."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item added successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Product not active or customerId missing"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CartItem> addItemLegacy(
        @Parameter(description = "Cart item request with customerId, productId and quantity", required = true)
        @Valid @RequestBody CartItemRequestWithCustomer request
    ) {
        CartItem item = cartService.addItemToCustomerCart(request.getCustomerId(),
            new CartItemRequest(request.getProductId(), request.getQuantity()));
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    /**
     * UC06: Update Cart - Update item quantity (customer ID in query - legacy OSGi compatibility)
     */
    @PutMapping("/items/{itemId}")
    @Operation(
        summary = "Update cart item quantity (legacy)",
        description = "UC06: Update Cart. FR-006: Update item quantity. OSGi compatibility endpoint with customerId in query parameter."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item updated successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found"),
        @ApiResponse(responseCode = "400", description = "Invalid quantity or customerId missing"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CartItem> updateCartItemLegacy(
        @Parameter(description = "Cart Item ID", required = true)
        @PathVariable Long itemId,
        @Parameter(description = "Customer ID", required = true)
        @RequestParam Long customerId,
        @Parameter(description = "New quantity (must be >= 1)", required = true)
        @RequestParam Integer quantity
    ) {
        Cart cart = cartService.getOrCreateCartByCustomerId(customerId);
        CartItem item = cartService.updateCartItemQuantity(cart.getId(), itemId, quantity);
        return ResponseEntity.ok(item);
    }

    /**
     * UC06: Update Cart - Remove item (customer ID in query - legacy OSGi compatibility)
     */
    @DeleteMapping("/items/{itemId}")
    @Operation(
        summary = "Remove item from cart (legacy)",
        description = "UC06: Update Cart. FR-006: Remove specific item from cart. OSGi compatibility endpoint with customerId in query parameter."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Item removed successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found"),
        @ApiResponse(responseCode = "400", description = "customerId missing"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> removeItemFromCartLegacy(
        @Parameter(description = "Cart Item ID", required = true)
        @PathVariable Long itemId,
        @Parameter(description = "Customer ID", required = true)
        @RequestParam Long customerId
    ) {
        Cart cart = cartService.getOrCreateCartByCustomerId(customerId);
        cartService.removeItemFromCart(cart.getId(), itemId);
        return ResponseEntity.noContent().build();
    }

    // ========== Cart Calculations Endpoints ==========

    /**
     * FR-007: Get cart total by cart ID
     */
    @GetMapping("/{cartId}/total")
    @Operation(
        summary = "Get cart total",
        description = "FR-007: Calculate and return cart total (sum of all item subtotals)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total calculated successfully"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BigDecimal> getCartTotal(
        @Parameter(description = "Cart ID", required = true)
        @PathVariable Long cartId
    ) {
        BigDecimal total = cartService.getCartTotal(cartId);
        return ResponseEntity.ok(total);
    }

    /**
     * FR-007: Get cart total by customer ID (query parameter - legacy OSGi compatibility)
     */
    @GetMapping(value = "/total", params = "customerId")
    @Operation(
        summary = "Get cart total by customer ID",
        description = "FR-007: Calculate and return cart total for customer. OSGi compatibility endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total calculated successfully"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BigDecimal> getCartTotalByCustomerId(
        @Parameter(description = "Customer ID", required = true)
        @RequestParam Long customerId
    ) {
        Cart cart = cartService.getOrCreateCartByCustomerId(customerId);
        BigDecimal total = cartService.getCartTotal(cart.getId());
        return ResponseEntity.ok(total);
    }

    /**
     * FR-007: Get cart item count by customer ID (legacy OSGi compatibility)
     */
    @GetMapping(value = "/count", params = "customerId")
    @Operation(
        summary = "Get cart item count",
        description = "FR-007: Get total count of items in customer's cart. OSGi compatibility endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Integer> getCartItemCount(
        @Parameter(description = "Customer ID", required = true)
        @RequestParam Long customerId
    ) {
        CartResponse response = cartService.getCartResponseByCustomerId(customerId);
        int count = response.getItems().stream()
            .mapToInt(item -> item.getQuantity())
            .sum();
        return ResponseEntity.ok(count);
    }

    /**
     * FR-008: Validate cart before checkout (legacy OSGi compatibility)
     */
    @PostMapping(value = "/validate", params = "customerId")
    @Operation(
        summary = "Validate cart",
        description = "FR-008: Validate cart items for checkout. Checks product availability and stock. OSGi compatibility endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation completed"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CartValidationResponse> validateCart(
        @Parameter(description = "Customer ID", required = true)
        @RequestParam Long customerId
    ) {
        CartValidationResponse validation = cartService.validateCart(customerId);
        return ResponseEntity.ok(validation);
    }

    // ========== Cart Merge Endpoint ==========

    /**
     * FR-009: Merge guest cart to customer cart
     */
    @PostMapping("/merge")
    @Operation(
        summary = "Merge guest cart to customer cart",
        description = "FR-009: Merge anonymous session cart to authenticated customer cart after login. Combines quantities if product exists in both carts."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carts merged successfully"),
        @ApiResponse(responseCode = "404", description = "Guest cart or customer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Cart> mergeGuestCartToCustomer(
        @Parameter(description = "Guest session ID", required = true)
        @RequestParam String sessionId,
        @Parameter(description = "Customer ID", required = true)
        @RequestParam Long customerId
    ) {
        Cart mergedCart = cartService.mergeGuestCartToCustomer(sessionId, customerId);
        return ResponseEntity.ok(mergedCart);
    }
}
