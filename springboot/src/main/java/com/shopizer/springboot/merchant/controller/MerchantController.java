package com.shopizer.springboot.merchant.controller;

import com.shopizer.springboot.merchant.dto.MerchantAuthResponse;
import com.shopizer.springboot.merchant.dto.MerchantLoginRequest;
import com.shopizer.springboot.merchant.dto.MerchantRegisterRequest;
import com.shopizer.springboot.merchant.dto.ProductAnalyticsResponse;
import com.shopizer.springboot.merchant.dto.ProductReportResponse;
import com.shopizer.springboot.merchant.entity.Merchant;
import com.shopizer.springboot.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopizer.springboot.merchant.dto.MerchantStoreRequest;
import com.shopizer.springboot.merchant.dto.MerchantStoreResponse;
import com.shopizer.springboot.merchant.dto.SalesReportResponse;
import com.shopizer.springboot.merchant.dto.InventoryItemResponse;
import com.shopizer.springboot.merchant.dto.InventoryUpdateRequest;
import com.shopizer.springboot.merchant.dto.MerchantProductCreateRequest;

import jakarta.validation.Valid;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Merchant REST Controller
 * FR-015: Merchant register and login
 */
@RestController
@RequestMapping("/api/v1/merchants")
@Tag(name = "Merchant", description = "Merchant management APIs (FR-015 to FR-018)")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    //FR-015 Merchant management endpoints
    @PostMapping
    @Operation(summary = "Register a new merchant", description = "FR-015: Merchant registration")
    public ResponseEntity<MerchantAuthResponse> registerMerchant(@Valid @RequestBody MerchantRegisterRequest request) {
        return new ResponseEntity<>(merchantService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Merchant login", description = "FR-015: Login and receive JWT token")
    public MerchantAuthResponse loginMerchant(@Valid @RequestBody MerchantLoginRequest request) {
        return merchantService.login(request);
    }

    @GetMapping
    @Operation(summary = "Get all merchants", description = "FR-015: List all merchants")
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        return ResponseEntity.ok(merchantService.getAllMerchants());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get merchant by ID", description = "FR-015: Get merchant details")
    public ResponseEntity<Merchant> getMerchantById(@PathVariable Long id) {
        return merchantService.getMerchantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update merchant", description = "FR-015: Update merchant details")
    public ResponseEntity<Merchant> updateMerchant(@PathVariable Long id, @RequestBody Merchant merchant) {
        return ResponseEntity.ok(merchantService.updateMerchant(id, merchant));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete merchant", description = "FR-015: Delete merchant")
    public ResponseEntity<Void> deleteMerchant(@PathVariable Long id) {
        merchantService.deleteMerchant(id);
        return ResponseEntity.noContent().build();
    }

    // FR-016 Store management endpoints

    // UC13 step2: list stores
    @GetMapping("/{merchantId}/stores")
    @Operation(summary = "Get store list", description = "FR-016: Get all stores for a merchant")
    public List<MerchantStoreResponse> listStores(@PathVariable Long merchantId) {
        return merchantService.listStores(merchantId);
    }

    // UC13 create store
    @PostMapping("/{merchantId}/stores")
    @Operation(summary = "Create a new store for a merchant", description = "FR-016: Create store")
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantStoreResponse createStore(
            @PathVariable Long merchantId,
            @Valid @RequestBody MerchantStoreRequest req
    ) {
        return merchantService.createStore(merchantId, req);
    }

    // View store details
    @GetMapping("/{merchantId}/stores/{storeId}")
    @Operation(summary = "Get store details", description = "FR-016: Get store details")
    public MerchantStoreResponse getStore(
            @PathVariable Long merchantId,
            @PathVariable Long storeId
    ) {
        return merchantService.getStore(merchantId, storeId);
    }

    // Update store details
    @PutMapping("/{merchantId}/stores/{storeId}")
    @Operation(summary = "Update store details", description = "FR-016: Update store")
    public MerchantStoreResponse updateStore(
            @PathVariable Long merchantId,
            @PathVariable Long storeId,
            @Valid @RequestBody MerchantStoreRequest req
    ) {
        return merchantService.updateStore(merchantId, storeId, req);
    }

    @DeleteMapping("/{merchantId}/stores/{storeId}")
    @Operation(summary = "Delete store", description = "FR-016: Delete store")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStore(@PathVariable Long merchantId, @PathVariable Long storeId) {
    merchantService.deleteStore(merchantId, storeId);
    }

    // FR-017 Inventory management endpoints

    //create product for a store
    @PostMapping("/{merchantId}/stores/{storeId}/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a product under a store",
            description = "FR-017: The system shall allow merchants to manage inventory (Create)"
    )
    public InventoryItemResponse createProduct(
            @PathVariable Long merchantId,
            @PathVariable Long storeId,
            @Valid @RequestBody MerchantProductCreateRequest req
    ) {
        return merchantService.createProduct(merchantId, storeId, req);
    }


    @GetMapping("/{merchantId}/inventory")
    @Operation(summary = "Get inventory for a merchant regardless store", description = "FR-017: The system shall allow merchants to manage inventory (Read)" )
    public List<InventoryItemResponse> getInventory(@PathVariable Long merchantId) {
        return merchantService.getInventory(merchantId);
    }

    //should add get inventory by store too
    @GetMapping("/{merchantId}/stores/{storeId}/inventory")
    @Operation(summary = "Get inventory for a specific store", description = "FR-017: The system shall allow merchants to manage inventory (Read)" )
    public List<InventoryItemResponse> getInventoryByStore(
            @PathVariable Long merchantId,
            @PathVariable Long storeId
    ) {
        return merchantService.getInventoryByStore(merchantId, storeId);
    }

    @PatchMapping("/{merchantId}/inventory/products/{productId}")
    @Operation(summary = "Update details for a product", description = "FR-017: The system shall allow merchants to manage inventory (Update)" )
    public InventoryItemResponse updateProduct(
            @PathVariable Long merchantId,
            @PathVariable Long productId,
            @Valid @RequestBody InventoryUpdateRequest req
    ) 
    {
        return merchantService.updateProduct(merchantId, productId, req);
    }

    @DeleteMapping("/{merchantId}/inventory/products/{productId}")
    @Operation(summary = "Delete a product", description = "FR-017: The system shall allow merchants to manage inventory (Delete)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable Long merchantId,
            @PathVariable Long productId
    ) {
        merchantService.deleteProduct(merchantId, productId);
    }

    @GetMapping("/{merchantId}/inventory/low-stock")
    @Operation(summary = "Get low stock products", description = "FR-018: Identify products needing restock")
    public List<InventoryItemResponse> getLowStockProducts(
            @PathVariable Long merchantId,
            @RequestParam(required = false) Long storeId
    ) {
        return merchantService.getLowStockProducts(merchantId, storeId);
    }

    //FR-018: Sales reports and analytics endpoints 

    // get Overview sales report
   @GetMapping("/{merchantId}/reports/sales")
    @Operation(
            summary = "Get sales report for merchant (optional store/date filter)",
            description = "FR-018: The system shall allow merchants to view sales reports and analytics"
    )
    public SalesReportResponse getSalesReport(
            @PathVariable Long merchantId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return merchantService.getSalesReport(merchantId, storeId, startDate, endDate);
    }

    @GetMapping("/{merchantId}/reports/products")
    @Operation(
            summary = "Get per-product sales report",
            description = "FR-018: Sales/orders per product with optional filters"
    )
    public List<ProductReportResponse> getProductReport(
            @PathVariable Long merchantId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return merchantService.getProductReport(merchantId, storeId, categoryId, productId, startDate, endDate);
    }

    @GetMapping("/{merchantId}/reports/products/{productId}")
    @Operation(
            summary = "Get detailed analytics for a product",
            description = "FR-018: Detailed product analytics including sales by day and conversion"
    )
    public ProductAnalyticsResponse getProductAnalytics(
            @PathVariable Long merchantId,
            @PathVariable Long productId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return merchantService.getProductAnalytics(merchantId, productId, startDate, endDate);
    }

    @PostMapping("/{merchantId}/products/{productId}/views")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Record a product view", description = "FR-018: Capture product view events for conversion tracking")
    public void recordProductView(
            @PathVariable Long merchantId,
            @PathVariable Long productId
    ) {
        merchantService.recordProductView(merchantId, productId);
    }


}

