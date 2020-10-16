package com.kaczmar.CurrencyAccount.dto;

import lombok.Data;
import org.hibernate.validator.constraints.pl.PESEL;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateUserAccountDto {

    @NotNull(message = "Please provide name")
    private String name;

    @NotNull(message = "Please provide surname")
    private String surname;

    @PESEL
    private String pesel;

    @NotNull
    private BigDecimal baseAccountAmount;

}
