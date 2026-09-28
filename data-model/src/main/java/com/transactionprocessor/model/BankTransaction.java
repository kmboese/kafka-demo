package com.transactionprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.Currency;

/**
 * A basic banking transaction moving money from one account to another.
 */
@Entity
@Table(name = "bank_transactions")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankTransaction implements Transaction, Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Positive
    @Column(name = "amount", nullable = false)
    private double amount;

    @NotNull
    @Column(name = "currency_code", length = 3, nullable = false)
    private Currency currency;

    @Valid
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "from_account_id", nullable = false)
    private BankAccount fromAccount;

    @Valid
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "to_account_id", nullable = false)
    private BankAccount toAccount;

    @Column(name = "created_at", insertable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;

    protected BankTransaction() {
    }

    public BankTransaction(Instant eventTime, double amount, Currency currency,
                           BankAccount fromAccount, BankAccount toAccount) {
        this.eventTime = eventTime;
        this.amount = amount;
        this.currency = currency;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BankAccount getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(BankAccount fromAccount) {
        this.fromAccount = fromAccount;
    }

    public BankAccount getToAccount() {
        return toAccount;
    }

    public void setToAccount(BankAccount toAccount) {
        this.toAccount = toAccount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "BankTransaction{id=" + id + ", eventTime=" + eventTime + ", amount=" + amount
                + ", currency=" + currency + ", from=" + fromAccount + ", to=" + toAccount + "}";
    }
}
