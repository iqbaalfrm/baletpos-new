package com.baletpos.model;

import java.math.BigDecimal;

/**
 * Model untuk Item Pembelian
 */
public class PurchaseItem {
    private Long id;
    private Long purchaseId;
    private Long productId;
    private String productSku;
    private String productName;
    private int quantity;
    private BigDecimal hppPerUnit;
    private BigDecimal subtotal;

    public PurchaseItem() {
        this.quantity = 1;
        this.hppPerUnit = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }

    public BigDecimal getHppPerUnit() {
        return hppPerUnit;
    }

    public void setHppPerUnit(BigDecimal hppPerUnit) {
        this.hppPerUnit = hppPerUnit;
        calculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void calculateSubtotal() {
        if (hppPerUnit != null) {
            this.subtotal = hppPerUnit.multiply(BigDecimal.valueOf(quantity));
        }
    }
}


