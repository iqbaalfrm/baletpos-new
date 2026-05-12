package com.baletpos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model untuk Pembelian (Purchase Order)
 */
public class Purchase {
    private Long id;
    private String purchaseNumber;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private LocalDateTime purchaseDate;
    private BigDecimal totalAmount;
    private String notes;
    private Status status;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private List<PurchaseItem> items;

    public enum Status {
        COMPLETED, RETURNED
    }

    public Purchase() {
        this.status = Status.COMPLETED;
        this.totalAmount = BigDecimal.ZERO;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPurchaseNumber() {
        return purchaseNumber;
    }

    public void setPurchaseNumber(String purchaseNumber) {
        this.purchaseNumber = purchaseNumber;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItem> items) {
        this.items = items;
    }

    public void addItem(PurchaseItem item) {
        this.items.add(item);
        calculateTotal();
    }

    public void removeItem(PurchaseItem item) {
        this.items.remove(item);
        calculateTotal();
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(PurchaseItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}


