package com.transactionprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Objects;

/**
 * A bank account, identified by institution + owner + account name.
 */
@Entity
@Table(name = "bank_account",
        uniqueConstraints = @UniqueConstraint(name = "uk_bank_account_natural_key",
                columnNames = {"institution_name", "owner_name", "account_name"}))
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "account_name", nullable = false)
    private String accountName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name = "created_at", insertable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;

    protected BankAccount() {
    }

    public BankAccount(String accountName, String ownerName, String institutionName) {
        this.accountName = accountName;
        this.ownerName = ownerName;
        this.institutionName = institutionName;
    }

    /**
     * Natural key used to match accounts across services and as the Kafka message key.
     */
    public String naturalKey() {
        return institutionName + "|" + ownerName + "|" + accountName;
    }

    public Integer getId() {
        return id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BankAccount that)) {
            return false;
        }
        return Objects.equals(accountName, that.accountName)
                && Objects.equals(ownerName, that.ownerName)
                && Objects.equals(institutionName, that.institutionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountName, ownerName, institutionName);
    }

    @Override
    public String toString() {
        return "BankAccount{" + naturalKey() + "}";
    }
}
