package com.shopizer.springboot.merchant.controller;

import com.shopizer.springboot.merchant.entity.Merchant;
import com.shopizer.springboot.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopizer.springboot.merchant.dto.MerchantStoreRequest;
import com.shopizer.springboot.merchant.dto.MerchantStoreResponse;
// import com.shopizer.springboot.merchant.service.MerchantService;
import jakarta.validation.Valid;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @Operation(summary = "Create a new merchant", description = "FR-015: Create merchant")
    public ResponseEntity<Merchant> createMerchant(@RequestBody Merchant merchant) {
        return new ResponseEntity<>(merchantService.createMerchant(merchant), HttpStatus.CREATED);
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

    // UC13 step2: list stores
    @GetMapping("/{merchantId}/stores")
    @Operation(summary = "Get store list", description = "FR-015: Get all stores for a merchant")
    public List<MerchantStoreResponse> listStores(@PathVariable Long merchantId) {
        return merchantService.listStores(merchantId);
    }

    // UC13 create store
    @PostMapping("/{merchantId}/stores")
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantStoreResponse createStore(
            @PathVariable Long merchantId,
            @Valid @RequestBody MerchantStoreRequest req
    ) {
        return merchantService.createStore(merchantId, req);
    }

    // View store details
    @GetMapping("/{merchantId}/stores/{storeId}")
    public MerchantStoreResponse getStore(
            @PathVariable Long merchantId,
            @PathVariable Long storeId
    ) {
        return merchantService.getStore(merchantId, storeId);
    }

    // Update store details
    @PutMapping("/{merchantId}/stores/{storeId}")
    public MerchantStoreResponse updateStore(
            @PathVariable Long merchantId,
            @PathVariable Long storeId,
            @Valid @RequestBody MerchantStoreRequest req
    ) {
        return merchantService.updateStore(merchantId, storeId, req);
    }

    @DeleteMapping("/{merchantId}/stores/{storeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStore(@PathVariable Long merchantId, @PathVariable Long storeId) {
    merchantService.deleteStore(merchantId, storeId);
}

}
