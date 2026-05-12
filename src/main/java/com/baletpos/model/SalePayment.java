package com.baletpos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalePayment {
    private Long id;
    private Long saleId;
    private String method;
    private BigDecimal amount;
    private String refNo;
    private String note;
    private LocalDateTime createdAt;

    public SalePayment() {
    }

    public SalePayment(String method, BigDecimal amount, String refNo) {
        this.method = method;
        this.amount = amount;
        this.refNo = refNo;
    }

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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
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
}


