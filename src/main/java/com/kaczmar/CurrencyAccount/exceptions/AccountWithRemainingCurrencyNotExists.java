package com.kaczmar.CurrencyAccount.exceptions;

public class AccountWithRemainingCurrencyNotExists extends Exception {

    public AccountWithRemainingCurrencyNotExists(String message) {
        super(message);
    }
}
