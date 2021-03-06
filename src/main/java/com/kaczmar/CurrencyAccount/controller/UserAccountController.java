package com.kaczmar.CurrencyAccount.controller;

import com.kaczmar.CurrencyAccount.dto.CreateUserAccountDto;
import com.kaczmar.CurrencyAccount.exceptions.NoUserFoundForGivenIdException;
import com.kaczmar.CurrencyAccount.exceptions.NoUserFoundForGivenPeselException;
import com.kaczmar.CurrencyAccount.model.RateResponse;
import com.kaczmar.CurrencyAccount.model.UserAccount;
import com.kaczmar.CurrencyAccount.model.UserAccountOutput;
import com.kaczmar.CurrencyAccount.service.RestTemplateService;
import com.kaczmar.CurrencyAccount.service.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/account")
@RestController
public class UserAccountController {


    private final UserAccountService userAccountService;

    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping
    public ResponseEntity<UserAccountOutput> createUser(@RequestBody CreateUserAccountDto userAccDto) throws Exception {
        UserAccount userAcc = userAccountService.createUserAcc(userAccDto);
        return new ResponseEntity<>(userAcc.convertFromUserAccountToOutput(), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAccountOutput> getUserById(@PathVariable("id") Long id) throws NoUserFoundForGivenIdException {
        UserAccount userAccountById = userAccountService.getUserById(id);
        return new ResponseEntity<>(userAccountById.convertFromUserAccountToOutput(), HttpStatus.FOUND);
    }

    @GetMapping
    public ResponseEntity<List<UserAccountOutput>> getAllUsers() {
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .body(userAccountService.getAllUsers().stream()
                        .map(e -> e.convertFromUserAccountToOutput())
                        .collect(Collectors.toList()));
    }

    @GetMapping("/pesel")
    public ResponseEntity<UserAccountOutput> getAccountByPesel(@RequestParam String pesel) throws NoUserFoundForGivenPeselException {
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .body(userAccountService.getUserByPesel(pesel).convertFromUserAccountToOutput());
    }




}

