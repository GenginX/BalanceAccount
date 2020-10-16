package com.kaczmar.CurrencyAccount.controller;

import com.kaczmar.CurrencyAccount.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CurrencyAccountAdvise {

    @ExceptionHandler(PeselAlreadyExistsException.class)
    public ResponseEntity<String> handleUserException(PeselAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(UserAgeIsNotLegalException.class)
    public ResponseEntity<String> handleUserException(UserAgeIsNotLegalException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(e.getMessage());
    }

    @ExceptionHandler(AccountWithRemainingCurrencyNotExists.class)
    public ResponseEntity<String> handleUsdCurrencyException(AccountWithRemainingCurrencyNotExists e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(NotEnoughMoneyOnAccount.class)
    public ResponseEntity<String> handleNotEnoughMoneyException(NotEnoughMoneyOnAccount e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(NoUserFoundForGivenIdException.class)
    public ResponseEntity<String> handleNoUserFoundForGivenIdException(NoUserFoundForGivenIdException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(NoUserFoundForGivenPeselException.class)
    public ResponseEntity<String> handleNoUserFoundForGivenPeselException(NoUserFoundForGivenPeselException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(PeselIsNotValid.class)
    public ResponseEntity<String> handlePeselIsNotValidException(PeselIsNotValid e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

}
