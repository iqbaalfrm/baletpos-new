package com.baletpos.model;

import java.time.LocalDateTime;
import java.util.List;

public class StockAdjustment {
    private Long id;
    private String adjNo;
    private Long userId;
    private Long productId;
    private Integer quantityChange;
    private String reason;
    private String notes;
    private LocalDateTime createdAt;
    private List<StockAdjustmentItem> items;

    // UI Helper
    private String createdByName;
    private String productSku;
    private String productName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdjNo() {
        return adjNo;
    }

    public void setAdjNo(String adjNo) {
        this.adjNo = adjNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<StockAdjustmentItem> getItems() {
        return items;
    }

    public void setItems(List<StockAdjustmentItem> items) {
        this.items = items;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
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


