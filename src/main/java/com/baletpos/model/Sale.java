package com.baletpos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private Long id;
    private String invoiceNumber;
    private Long customerId;
    private LocalDateTime saleDate;
    private BigDecimal subtotal;
    private Double discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal totalHpp;
    private PaymentMethod paymentMethod;
    private PaymentType paymentType = PaymentType.SINGLE;
    private BigDecimal paymentAmount;
    private BigDecimal changeAmount;
    private Status status;
    private String voidReason;
    private Long voidedBy;
    private LocalDateTime voidedAt;
    private String notes;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long technicianId;

    private List<SaleItem> items = new ArrayList<>();
    private List<SalePayment> payments = new ArrayList<>();

    private String customerName; // Transient
    private String cashierName; // Transient
    private String createdByName; // Transient

    public enum PaymentMethod {
        CASH("Cash"),
        TRANSFER_BCA("Transfer BCA"),
        TRANSFER_MANDIRI("Transfer Mandiri"),
        DEBIT_BRI("Debit BRI"),
        AKULAKU("Akulaku"),
        KREDIVO("Kredivo"),
        SPAYLATER("Spaylater"),
        QRIS("QRIS"),
        PENGADAAN_CV("Pengadaan CV"),
        SPLIT("Split");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum PaymentType {
        SINGLE, SPLIT
    }

    public enum Status {
        COMPLETED, VOIDED, RETURNED
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalHpp() {
        return totalHpp;
    }

    public void setTotalHpp(BigDecimal totalHpp) {
        this.totalHpp = totalHpp;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    public Long getVoidedBy() {
        return voidedBy;
    }

    public void setVoidedBy(Long voidedBy) {
        this.voidedBy = voidedBy;
    }

    public LocalDateTime getVoidedAt() {
        return voidedAt;
    }

    public void setVoidedAt(LocalDateTime voidedAt) {
        this.voidedAt = voidedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }

    public void addItem(SaleItem item) {
        this.items.add(item);
    }

    public List<SalePayment> getPayments() {
        return payments;
    }

    public void setPayments(List<SalePayment> payments) {
        this.payments = payments;
    }

    public void addPayment(SalePayment payment) {
        this.payments.add(payment);
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
}


