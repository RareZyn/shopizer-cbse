package com.shopizer.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shipping_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private MerchantStore store;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal cost;

    @Column(nullable = false)
    private Integer estimatedDays;

    @Column(nullable = false)
    private Boolean active = true;
}
