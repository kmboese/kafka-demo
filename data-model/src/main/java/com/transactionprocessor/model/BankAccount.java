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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * A bank account, identified by institution + owner + account name.
 */
@Entity
@Table(name = "bank_account",
        uniqueConstraints = @UniqueConstraint(name = "uk_bank_account_natural_key",
                columnNames = {"institution_name", "owner_name", "account_name"}))
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "account_name", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String accountName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "owner_name", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String ownerName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "institution_name", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String institutionName;

    @Column(name = "created_at", insertable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;

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
}
