package com.kaczmar.CurrencyAccount.exceptions;

public class NoUserFoundForGivenPeselException extends Exception {

    public NoUserFoundForGivenPeselException(String message) {
        super(message);
    }
}
