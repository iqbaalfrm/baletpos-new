package com.baletpos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SalesReturn {
    private Long id;
    private String returnNo;
    private Long saleId;
    private Long userId;
    private LocalDateTime createdAt;
    private String notes;
    private BigDecimal totalAmount;
    private List<SalesReturnItem> items;

    // UI Helpers
    private String saleInvoiceNumber;
    private String createdByName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReturnNo() {
        return returnNo;
    }

    public void setReturnNo(String returnNo) {
        this.returnNo = returnNo;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<SalesReturnItem> getItems() {
        return items;
    }

    public void setItems(List<SalesReturnItem> items) {
        this.items = items;
    }

    public String getSaleInvoiceNumber() {
        return saleInvoiceNumber;
    }

    public void setSaleInvoiceNumber(String saleInvoiceNumber) {
        this.saleInvoiceNumber = saleInvoiceNumber;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
}


