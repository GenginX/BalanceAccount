package com.kaczmar.CurrencyAccount.exceptions;

public class PeselAlreadyExistsException extends Exception {

    public PeselAlreadyExistsException(String message) {
        super(message);
    }
}
