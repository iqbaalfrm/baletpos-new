package com.baletpos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesReturnItem {
    private Long id;
    private Long salesReturnId;
    private Long saleItemId;
    private Long productId;
    private int qtyReturn;
    private BigDecimal unitPrice;
    private BigDecimal snapshotHpp;
    private BigDecimal lineTotal;
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

    public Long getSalesReturnId() {
        return salesReturnId;
    }

    public void setSalesReturnId(Long salesReturnId) {
        this.salesReturnId = salesReturnId;
    }

    public Long getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(Long saleItemId) {
        this.saleItemId = saleItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQtyReturn() {
        return qtyReturn;
    }

    public void setQtyReturn(int qtyReturn) {
        this.qtyReturn = qtyReturn;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSnapshotHpp() {
        return snapshotHpp;
    }

    public void setSnapshotHpp(BigDecimal snapshotHpp) {
        this.snapshotHpp = snapshotHpp;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
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


