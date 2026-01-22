package com.shopizer.cart.impl;

import com.shopizer.cart.api.CartService;
import com.shopizer.cart.dto.*;
import com.shopizer.cart.repository.CartItemRepository;
import com.shopizer.cart.repository.CartRepository;
import com.shopizer.catalog.api.CatalogService;
import com.shopizer.catalog.dto.ProductResponse;
import com.shopizer.common.entity.Cart;
import com.shopizer.common.entity.CartItem;
import com.shopizer.common.entity.Customer;
import com.shopizer.common.entity.Product;
import com.shopizer.common.exception.BadRequestException;
import com.shopizer.common.exception.InsufficientStockException;
import com.shopizer.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CatalogService catalogService;

    public CartServiceImpl(CartRepository cartRepository,
                          CartItemRepository cartItemRepository,
                          CatalogService catalogService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.catalogService = catalogService;
    }

    // ========== Cart Management ==========

    @Override
    public Cart createCart(Cart cart) {
        logger.info("Creating new cart");
        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> getAllCarts() {
        logger.info("Getting all carts");
        return cartRepository.findAll();
    }

    @Override
    public Optional<Cart> getCartById(Long id) {
        logger.info("Getting cart by ID: {}", id);
        return cartRepository.findById(id);
    }

    @Override
    public void deleteCartById(Long id) {
        logger.info("Deleting cart by ID: {}", id);
        Cart cart = cartRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", id));
        cartRepository.delete(cart);
    }

    @Override
    public void clearCartById(Long cartId) {
        logger.info("Clearing cart by ID: {}", cartId);
        cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
        cartItemRepository.deleteByCartId(cartId);
    }

    @Override
    public CartResponse getCartByCustomerId(Long customerId) {
        logger.info("Getting cart by customer ID: {}", customerId);
        Cart cart = getOrCreateCart(customerId);
        return mapToCartResponse(cart);
    }

    @Override
    public BigDecimal getCartTotalById(Long cartId) {
        logger.info("Getting cart total by cart ID: {}", cartId);
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        return items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========== Cart Item Management by Cart ID ==========

    @Override
    public CartResponse addItemToCart(Long cartId, CartItemRequest request) {
        logger.info("Adding item to cart ID: {}, product: {}", cartId, request.getProductId());

        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        // Validate product exists and has stock
        ProductResponse product = catalogService.getProductById(request.getProductId());
        if (!catalogService.checkStock(request.getProductId(), request.getQuantity())) {
            throw new InsufficientStockException(
                product.getName(),
                request.getQuantity(),
                product.getStockQuantity()
            );
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            if (!catalogService.checkStock(request.getProductId(), newQuantity)) {
                throw new InsufficientStockException(
                    product.getName(),
                    newQuantity,
                    product.getStockQuantity()
                );
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);

            Product productEntity = new Product();
            productEntity.setId(request.getProductId());
            newItem.setProduct(productEntity);

            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(request.getPrice() != null ? request.getPrice() : product.getPrice());

            cartItemRepository.save(newItem);
        }

        return mapToCartResponseByCartId(cartId);
    }

    @Override
    public CartResponse updateCartItemById(Long cartId, Long itemId, Integer quantity) {
        logger.info("Updating cart item {} in cart {} to quantity {}", itemId, cartId, quantity);

        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this cart");
        }

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        // Check stock availability
        if (!catalogService.checkStock(item.getProduct().getId(), quantity)) {
            ProductResponse product = catalogService.getProductById(item.getProduct().getId());
            throw new InsufficientStockException(
                product.getName(),
                quantity,
                product.getStockQuantity()
            );
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return mapToCartResponseByCartId(cartId);
    }

    @Override
    public void removeItemFromCartById(Long cartId, Long itemId) {
        logger.info("Removing cart item {} from cart {}", itemId, cartId);

        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this cart");
        }

        cartItemRepository.delete(item);
    }

    @Override
    public CartResponse addItemToCustomerCart(Long customerId, CartItemRequest request) {
        return addToCart(customerId, request);
    }

    // ========== Customer-centric Operations ==========

    @Override
    public CartResponse addToCart(Long customerId, CartItemRequest request) {
        logger.info("Adding item to cart for customer: {}, product: {}", customerId, request.getProductId());

        // Validate product exists and has stock
        ProductResponse product = catalogService.getProductById(request.getProductId());
        if (!catalogService.checkStock(request.getProductId(), request.getQuantity())) {
            throw new InsufficientStockException(
                product.getName(),
                request.getQuantity(),
                product.getStockQuantity()
            );
        }

        // Get or create cart
        Cart cart = getOrCreateCart(customerId);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            if (!catalogService.checkStock(request.getProductId(), newQuantity)) {
                throw new InsufficientStockException(
                    product.getName(),
                    newQuantity,
                    product.getStockQuantity()
                );
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);

            Product productEntity = new Product();
            productEntity.setId(request.getProductId());
            newItem.setProduct(productEntity);

            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(request.getPrice() != null ? request.getPrice() : product.getPrice());

            cartItemRepository.save(newItem);
        }

        return viewCart(customerId);
    }

    @Override
    public CartResponse viewCart(Long customerId) {
        logger.info("Viewing cart for customer: {}", customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItem(Long customerId, Long itemId, Integer quantity) {
        logger.info("Updating cart item {} for customer {} to quantity {}", itemId, customerId, quantity);

        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this customer");
        }

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        // Check stock availability
        if (!catalogService.checkStock(item.getProduct().getId(), quantity)) {
            ProductResponse product = catalogService.getProductById(item.getProduct().getId());
            throw new InsufficientStockException(
                product.getName(),
                quantity,
                product.getStockQuantity()
            );
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return viewCart(customerId);
    }

    @Override
    public void removeFromCart(Long customerId, Long itemId) {
        logger.info("Removing cart item {} for customer {}", itemId, customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this customer");
        }

        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart(Long customerId) {
        logger.info("Clearing cart for customer: {}", customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

        cartItemRepository.deleteByCartId(cart.getId());
    }

    @Override
    public BigDecimal calculateTotal(Long customerId) {
        logger.info("Calculating total for customer: {}", customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        return items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public CartValidationResponse validateCart(Long customerId) {
        logger.info("Validating cart for customer: {}", customerId);

        CartValidationResponse validation = CartValidationResponse.valid();

        try {
            Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

            List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

            if (items.isEmpty()) {
                validation.addError("Cart is empty");
                return validation;
            }

            // Validate each item
            for (CartItem item : items) {
                try {
                    ProductResponse product = catalogService.getProductById(item.getProduct().getId());

                    // Check if product is active
                    if (!product.getActive()) {
                        validation.addError(String.format("Product '%s' is no longer available", product.getName()));
                    }

                    // Check stock availability
                    if (!catalogService.checkStock(item.getProduct().getId(), item.getQuantity())) {
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
                    if (item.getPrice().compareTo(product.getPrice()) != 0) {
                        validation.addWarning(String.format(
                            "Price changed for '%s'. Cart price: %s, Current price: %s",
                            product.getName(),
                            item.getPrice(),
                            product.getPrice()
                        ));
                    }

                } catch (ResourceNotFoundException e) {
                    validation.addError(String.format("Product with ID %d no longer exists", item.getProduct().getId()));
                }
            }

        } catch (ResourceNotFoundException e) {
            validation.addError("Cart not found");
        }

        return validation;
    }

    @Override
    public Integer getCartItemCount(Long customerId) {
        logger.info("Getting cart item count for customer: {}", customerId);

        try {
            Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

            List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
            return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        } catch (ResourceNotFoundException e) {
            return 0;
        }
    }

    @Override
    public CartResponse mergeCart(Long anonymousCartId, Long customerId) {
        logger.info("Merging anonymous cart {} with customer cart {}", anonymousCartId, customerId);

        Cart anonymousCart = cartRepository.findById(anonymousCartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", anonymousCartId));

        Cart customerCart = getOrCreateCart(customerId);

        List<CartItem> anonymousItems = cartItemRepository.findByCartId(anonymousCartId);

        for (CartItem anonymousItem : anonymousItems) {
            Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(customerCart.getId(), anonymousItem.getProduct().getId());

            if (existingItem.isPresent()) {
                // Merge quantities
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + anonymousItem.getQuantity());
                cartItemRepository.save(item);
            } else {
                // Move item to customer cart
                anonymousItem.setCart(customerCart);
                cartItemRepository.save(anonymousItem);
            }
        }

        // Delete anonymous cart
        cartRepository.delete(anonymousCart);

        return viewCart(customerId);
    }

    // Helper methods
    private Cart getOrCreateCart(Long customerId) {
        Optional<Cart> existingCart = cartRepository.findByCustomerId(customerId);

        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create new cart
        Cart cart = new Cart();
        Customer customer = new Customer();
        customer.setId(customerId);
        cart.setCustomer(customer);

        return cartRepository.save(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setCustomerId(cart.getCustomer() != null ? cart.getCustomer().getId() : null);
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = items.stream()
            .map(this::mapToCartItemResponse)
            .collect(Collectors.toList());

        response.setItems(itemResponses);
        response.setTotalItems(items.stream().mapToInt(CartItem::getQuantity).sum());

        BigDecimal subtotal = items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setSubtotal(subtotal);
        response.setTotal(subtotal);

        return response;
    }

    private CartResponse mapToCartResponseByCartId(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
        return mapToCartResponse(cart);
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setSubtotal(item.getSubtotal());

        try {
            ProductResponse product = catalogService.getProductById(item.getProduct().getId());
            response.setProductName(product.getName());
            response.setProductImageUrl(product.getImageUrl());
            response.setInStock(catalogService.checkStock(product.getId(), item.getQuantity()));
            response.setAvailableStock(product.getStockQuantity());
        } catch (ResourceNotFoundException e) {
            response.setProductName("Product Not Found");
            response.setInStock(false);
            response.setAvailableStock(0);
        }

        return response;
    }
}
