package com.baletpos.model;

import java.math.BigDecimal;

public class SaleItem {
    private Long id;
    private Long saleId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal hppPerUnit;
    private Double discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal subtotal;

    private String productName; // Transient
    private String productSku; // Transient
    private String serialNumber; // Transient (Input by user)
    private String buyerNik; // Transient - NIK pembeli (untuk laptop)
    private String buyerName; // Transient - Nama pembeli (untuk laptop)
    private Long bonusProductId; // Bonus peripheral untuk pembelian laptop
    private String bonusProductName; // Transient/display
    private String warrantyLabel; // Garansi laptop
    private Product.ProductType productType; // Transient

    public SaleItem() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getHppPerUnit() {
        return hppPerUnit;
    }

    public void setHppPerUnit(BigDecimal hppPerUnit) {
        this.hppPerUnit = hppPerUnit;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBuyerNik() {
        return buyerNik;
    }

    public void setBuyerNik(String buyerNik) {
        this.buyerNik = buyerNik;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Long getBonusProductId() {
        return bonusProductId;
    }

    public void setBonusProductId(Long bonusProductId) {
        this.bonusProductId = bonusProductId;
    }

    public String getBonusProductName() {
        return bonusProductName;
    }

    public void setBonusProductName(String bonusProductName) {
        this.bonusProductName = bonusProductName;
    }

    public String getWarrantyLabel() {
        return warrantyLabel;
    }

    public void setWarrantyLabel(String warrantyLabel) {
        this.warrantyLabel = warrantyLabel;
    }

    public Product.ProductType getProductType() {
        return productType;
    }

    public void setProductType(Product.ProductType productType) {
        this.productType = productType;
    }
}


