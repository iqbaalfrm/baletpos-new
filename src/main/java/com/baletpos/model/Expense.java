package com.baletpos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model untuk Biaya Operasional
 */
public class Expense {
    private Long id;
    private String expenseNumber;
    private Long expenseCodeId;
    private String expenseCode;
    private String expenseCodeName;
    private LocalDate expenseDate;
    private BigDecimal amount;
    private String description;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;

    public Expense() {
        this.amount = BigDecimal.ZERO;
        this.expenseDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExpenseNumber() {
        return expenseNumber;
    }

    public void setExpenseNumber(String expenseNumber) {
        this.expenseNumber = expenseNumber;
    }

    public Long getExpenseCodeId() {
        return expenseCodeId;
    }

    public void setExpenseCodeId(Long expenseCodeId) {
        this.expenseCodeId = expenseCodeId;
    }

    public String getExpenseCode() {
        return expenseCode;
    }

    public void setExpenseCode(String expenseCode) {
        this.expenseCode = expenseCode;
    }

    public String getExpenseCodeName() {
        return expenseCodeName;
    }

    public void setExpenseCodeName(String expenseCodeName) {
        this.expenseCodeName = expenseCodeName;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}


