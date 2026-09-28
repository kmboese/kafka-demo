package com.transactionprocessor.model;

import java.util.Currency;

/**
 * Any financial transaction.
 */
public interface Transaction {

    double getAmount();

    Currency getCurrency();
}
