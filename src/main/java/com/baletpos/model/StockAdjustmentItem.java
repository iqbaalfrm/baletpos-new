package com.baletpos.model;

import java.time.LocalDateTime;

public class StockAdjustmentItem {
    private Long id;
    private Long stockAdjustmentId;
    private Long productId;
    private int qtyDelta;
    private String note;
    private LocalDateTime createdAt;

    // UI Helpers
    private String productSku;
    private String productName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStockAdjustmentId() {
        return stockAdjustmentId;
    }

    public void setStockAdjustmentId(Long stockAdjustmentId) {
        this.stockAdjustmentId = stockAdjustmentId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQtyDelta() {
        return qtyDelta;
    }

    public void setQtyDelta(int qtyDelta) {
        this.qtyDelta = qtyDelta;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
}


