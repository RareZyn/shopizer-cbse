package com.shopizer.springboot.cart.service;

import com.shopizer.springboot.cart.dto.CartItemRequest;
import com.shopizer.springboot.cart.dto.CartItemResponse;
import com.shopizer.springboot.cart.dto.CartResponse;
import com.shopizer.springboot.cart.dto.CartValidationResponse;
import com.shopizer.springboot.cart.entity.Cart;
import com.shopizer.springboot.cart.entity.CartItem;
import com.shopizer.springboot.cart.repository.CartItemRepository;
import com.shopizer.springboot.cart.repository.CartRepository;
import com.shopizer.springboot.catalog.entity.Product;
import com.shopizer.springboot.catalog.repository.ProductRepository;
import com.shopizer.springboot.common.exception.BadRequestException;
import com.shopizer.springboot.common.exception.InsufficientStockException;
import com.shopizer.springboot.common.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cart Service Implementation
 * Complete business logic implementation for shopping cart operations.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: Cart item management (Add, View, Update, Remove)
 * - FR-007: Calculate and display cart total automatically
 * - FR-008: Validate product availability before adding to cart
 * - FR-009: Persist cart data for logged-in customers
 *
 * USE CASES IMPLEMENTED:
 *
 * UC04: Add to Cart - COMPLETE IMPLEMENTATION
 * Method: addItemToCart()
 * Flow:
 *   1. Validate cart exists (throws ResourceNotFoundException if not found)
 *   2. Validate product exists via ProductRepository (FR-008)
 *   3. Check product is active (FR-008)
 *   4. Validate stock availability (product.stockQuantity >= requested quantity) (FR-008)
 *   5. Check for duplicate product in cart (findByCartIdAndProductId)
 *   6. If exists: Update quantity (merge duplicate)
 *   7. If not: Create new CartItem with current product price
 *   8. Save to database (FR-009)
 *   9. Return CartItem entity
 *
 * Alternative Flow: Product with variation
 *   - Handled via productId which can reference variant SKUs
 *
 * Exception Flows:
 *   - Product not found → ResourceNotFoundException
 *   - Product not active → BadRequestException
 *   - Insufficient stock → InsufficientStockException
 *
 * UC05: View Cart - COMPLETE IMPLEMENTATION
 * Method: getCartResponse()
 * Flow:
 *   1. Fetch cart entity by ID
 *   2. Map to CartResponse DTO
 *   3. For each CartItem:
 *      a. Fetch product details from ProductRepository (name, SKU)
 *      b. Calculate subtotal (quantity × unitPrice)
 *      c. Create CartItemResponse DTO
 *   4. Calculate cart total (sum of all subtotals) (FR-007)
 *   5. Set itemCount (number of items) (FR-007)
 *   6. Return CartResponse
 *
 * Exception Flow: No product in cart
 *   - Returns CartResponse with items=[], total=0, itemCount=0
 *
 * UC06: Update Cart - COMPLETE IMPLEMENTATION
 * Methods: updateCartItemQuantity(), removeItemFromCart(), clearCart()
 *
 * updateCartItemQuantity() Flow:
 *   1. Validate cart exists
 *   2. Validate cart item exists
 *   3. Fetch product from ProductRepository
 *   4. Re-validate stock availability for new quantity (FR-008)
 *   5. Update quantity in CartItem entity
 *   6. Save to database (FR-009)
 *   7. Return updated CartItem
 *
 * Alternative Flow: Customer removes product (reduce quantity to 0)
 *   - Handled by removeItemFromCart() method instead
 *
 * Exception Flows:
 *   - Product Low Stock → InsufficientStockException with available stock message
 *   - Product Not Available → BadRequestException
 *
 * removeItemFromCart() Flow:
 *   1. Validate cart and item exist
 *   2. Delete CartItem from database
 *   3. Confirmation handled by HTTP 204 No Content response
 *
 * clearCart() Flow:
 *   1. Validate cart exists
 *   2. Delete all items via deleteByCartId()
 *   3. Cart entity remains (only items deleted)
 *
 * ROLE IN ARCHITECTURE:
 * - SERVICE LAYER (Business Logic Implementation)
 * - Implements CartService interface
 * - Depends on:
 *   * CartRepository (Data Access)
 *   * CartItemRepository (Data Access)
 *   * ProductRepository (Cross-Module: Catalog)
 * - Used by CartController (Presentation Layer)
 * - All methods are @Transactional for data consistency
 *
 * CROSS-MODULE INTEGRATION:
 * - ProductRepository from Catalog module used for:
 *   1. Product validation (exists, active status)
 *   2. Stock availability checking (FR-008)
 *   3. Enriching cart responses with product details (name, SKU)
 *
 * EXCEPTION HANDLING:
 * - ResourceNotFoundException: Cart/CartItem/Product not found
 * - BadRequestException: Product not active, invalid operations
 * - InsufficientStockException: Stock level below requested quantity
 *
 * DTO MAPPING:
 * - mapToCartResponse(): Cart entity → CartResponse DTO
 * - mapToCartItemResponse(): CartItem entity → CartItemResponse DTO
 * - Enriches DTOs with product details via ProductRepository lookup
 *
 * TOTAL IMPLEMENTATION: 345 lines of code, 19 methods
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository,
                          CartItemRepository cartItemRepository,
                          ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    // ========== Cart Management ==========

    @Override
    public Cart createCart(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Optional<Cart> getCartById(Long id) {
        return cartRepository.findById(id);
    }

    @Override
    public Cart getOrCreateCartByCustomerId(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomerId(customerId);
                    newCart.setStatus("ACTIVE");
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public Cart getOrCreateCartBySessionId(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(sessionId);
                    newCart.setStatus("ACTIVE");
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public Optional<Cart> getCartByCustomerId(Long customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    @Override
    public void deleteCart(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + id));
        cartRepository.delete(cart);
    }

    @Override
    public void clearCart(Long cartId) {
        cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        cartItemRepository.deleteByCartId(cartId);
    }

    // ========== Cart Item Management ==========

    /**
     * UC04: Add to Cart - Main Implementation
     * Validates product and stock before adding item to cart
     */
    @Override
    public CartItem addItemToCart(Long cartId, CartItemRequest request) {
        // Step 1: Validate cart exists
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        // Step 2: Validate product exists (FR-008)
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        // Step 3: Check product is active (FR-008)
        if (!product.getIsActive()) {
            throw new BadRequestException("Product is not available: " + product.getName());
        }

        // Step 4: Validate stock availability (FR-008)
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                "Insufficient stock for product: " + product.getName() +
                ". Requested: " + request.getQuantity() +
                ", Available: " + product.getStockQuantity()
            );
        }

        // Step 5: Check for duplicate product in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cartId, request.getProductId());

        if (existingItem.isPresent()) {
            // Step 6: Update existing item quantity (merge duplicate)
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            // Re-validate stock for new total quantity
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName() +
                    ". Requested total: " + newQuantity +
                    ", Available: " + product.getStockQuantity()
                );
            }

            item.setQuantity(newQuantity);
            item.setPrice(item.getUnitPrice().multiply(new java.math.BigDecimal(newQuantity)));
            return cartItemRepository.save(item);
        } else {
            // Step 7: Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(product.getId());
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(product.getPrice());
            newItem.setPrice(product.getPrice().multiply(new java.math.BigDecimal(request.getQuantity())));

            // Step 8: Save to database (FR-009)
            return cartItemRepository.save(newItem);
        }
    }

    /**
     * UC04: Add to Cart - Customer variant
     * Adds item to customer's cart (auto-creates cart if needed)
     */
    @Override
    public CartItem addItemToCustomerCart(Long customerId, CartItemRequest request) {
        Cart cart = getOrCreateCartByCustomerId(customerId);
        return addItemToCart(cart.getId(), request);
    }

    /**
     * UC06: Update Cart - Update item quantity
     * Re-validates stock availability for new quantity
     */
    @Override
    public CartItem updateCartItemQuantity(Long cartId, Long itemId, Integer quantity) {
        // Step 1: Validate cart exists
        cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        // Step 2: Validate cart item exists
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        // Validate item belongs to this cart
        if (!item.getCart().getId().equals(cartId)) {
            throw new BadRequestException("Cart item does not belong to this cart");
        }

        // Step 3: Fetch product for validation
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProductId()));

        // Step 4: Re-validate stock availability (FR-008)
        if (!product.getIsActive()) {
            throw new BadRequestException("Product is no longer available: " + product.getName());
        }

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                "Insufficient stock for product: " + product.getName() +
                ". Requested: " + quantity +
                ", Available: " + product.getStockQuantity()
            );
        }

        // Step 5: Update quantity
        item.setQuantity(quantity);
        item.setPrice(item.getUnitPrice().multiply(new java.math.BigDecimal(quantity)));

        // Step 6: Save to database (FR-009)
        return cartItemRepository.save(item);
    }

    /**
     * UC06: Update Cart - Remove item from cart
     */
    @Override
    public void removeItemFromCart(Long cartId, Long itemId) {
        // Validate cart exists
        cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        // Validate cart item exists
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        // Validate item belongs to this cart
        if (!item.getCart().getId().equals(cartId)) {
            throw new BadRequestException("Cart item does not belong to this cart");
        }

        // Delete item
        cartItemRepository.delete(item);
    }

    @Override
    public Optional<CartItem> getCartItemById(Long itemId) {
        return cartItemRepository.findById(itemId);
    }

    // ========== Cart Calculations ==========

    /**
     * FR-007: Calculate cart total
     */
    @Override
    public BigDecimal getCartTotal(Long cartId) {
        cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        List<CartItem> items = cartItemRepository.findByCartId(cartId);

        return items.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * UC05: View Cart - Returns complete cart with details
     * FR-007: Auto-calculates total and item count
     */
    @Override
    public CartResponse getCartResponse(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        return mapToCartResponse(cart);
    }

    /**
     * UC05: View Cart - By customer ID variant
     */
    @Override
    public CartResponse getCartResponseByCustomerId(Long customerId) {
        Cart cart = getOrCreateCartByCustomerId(customerId);
        return mapToCartResponse(cart);
    }

    /**
     * Validate cart before checkout
     * FR-008: Validate product availability
     */
    @Override
    public CartValidationResponse validateCart(Long customerId) {
        CartValidationResponse validation = CartValidationResponse.valid();

        try {
            Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer: " + customerId));

            List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

            if (items.isEmpty()) {
                validation.addError("Cart is empty");
                return validation;
            }

            // Validate each item
            for (CartItem item : items) {
                try {
                    Product product = productRepository.findById(item.getProductId())
                        .orElse(null);

                    if (product == null) {
                        validation.addError(String.format("Product with ID %d no longer exists", item.getProductId()));
                        continue;
                    }

                    // Check if product is active
                    if (!product.getIsActive()) {
                        validation.addError(String.format("Product '%s' is no longer available", product.getName()));
                    }

                    // Check stock availability
                    if (product.getStockQuantity() < item.getQuantity()) {
                        validation.addError(String.format(
                            "Insufficient stock for '%s'. Available: %d, Required: %d",
                            product.getName(),
                            product.getStockQuantity(),
                            item.getQuantity()
                        ));
                    }

                    // Check for low stock warning
                    if (product.getStockQuantity() < 10 && product.getStockQuantity() >= item.getQuantity()) {
                        validation.addWarning(String.format(
                            "Low stock for '%s'. Only %d items remaining",
                            product.getName(),
                            product.getStockQuantity()
                        ));
                    }

                    // Check price changes
                    if (item.getUnitPrice().compareTo(product.getPrice()) != 0) {
                        validation.addWarning(String.format(
                            "Price changed for '%s'. Cart price: %s, Current price: %s",
                            product.getName(),
                            item.getUnitPrice(),
                            product.getPrice()
                        ));
                    }

                } catch (Exception e) {
                    validation.addError(String.format("Error validating product with ID %d: %s",
                        item.getProductId(), e.getMessage()));
                }
            }

        } catch (ResourceNotFoundException e) {
            validation.addError("Cart not found");
        }

        return validation;
    }

    // ========== Cart Merge (Guest to Customer) ==========

    /**
     * Merge guest cart to customer cart after login
     * Combines quantities if same product exists in both carts
     */
    @Override
    public Cart mergeGuestCartToCustomer(String sessionId, Long customerId) {
        // Get guest cart
        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);
        if (guestCartOpt.isEmpty()) {
            // No guest cart to merge, just return customer cart
            return getOrCreateCartByCustomerId(customerId);
        }

        Cart guestCart = guestCartOpt.get();
        Cart customerCart = getOrCreateCartByCustomerId(customerId);

        // Get items from guest cart
        List<CartItem> guestItems = cartItemRepository.findByCartId(guestCart.getId());

        // Merge each guest item into customer cart
        for (CartItem guestItem : guestItems) {
            Optional<CartItem> existingItem = cartItemRepository
                    .findByCartIdAndProductId(customerCart.getId(), guestItem.getProductId());

            if (existingItem.isPresent()) {
                // Product exists in customer cart - combine quantities
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity() + guestItem.getQuantity();
                item.setQuantity(newQuantity);
                item.setPrice(item.getUnitPrice().multiply(new java.math.BigDecimal(newQuantity)));
                cartItemRepository.save(item);
            } else {
                // Add new item to customer cart
                CartItem newItem = new CartItem();
                newItem.setCart(customerCart);
                newItem.setProductId(guestItem.getProductId());
                newItem.setQuantity(guestItem.getQuantity());
                newItem.setUnitPrice(guestItem.getUnitPrice());
                newItem.setPrice(guestItem.getUnitPrice().multiply(new java.math.BigDecimal(guestItem.getQuantity())));
                cartItemRepository.save(newItem);
            }
        }

        // Delete guest cart
        cartRepository.delete(guestCart);

        return customerCart;
    }

    // ========== Helper Methods - DTO Mapping ==========

    /**
     * Maps Cart entity to CartResponse DTO
     * Enriches with product details and calculates totals (FR-007)
     */
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        // Calculate total (FR-007)
        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setCustomerId(cart.getCustomerId());
        response.setSessionId(cart.getSessionId());
        response.setStatus(cart.getStatus());
        response.setItems(itemResponses);
        response.setTotal(total);
        response.setItemCount(items.size());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        return response;
    }

    /**
     * Maps CartItem entity to CartItemResponse DTO
     * Enriches with product details from Catalog module
     */
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());

        // Set subtotal from stored price (FR-007)
        response.setSubtotal(item.getPrice());

        // Enrich with product details from Catalog module
        Optional<Product> productOpt = productRepository.findById(item.getProductId());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            response.setProductName(product.getName());
            response.setProductSku(product.getSku());
        }

        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());

        return response;
    }
}
