package com.kaczmar.CurrencyAccount.exceptions;

public class NotEnoughMoneyOnAccount extends Exception {

    public NotEnoughMoneyOnAccount(String message) {
        super(message);
    }
}
