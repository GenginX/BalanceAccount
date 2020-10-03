package com.kaczmar.CurrencyAccount.dto;

import lombok.Data;
import org.hibernate.validator.constraints.pl.PESEL;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateUserSubAccountDto {

    @PESEL
    private String pesel;

    @NotNull
    private String currency;

    @NotNull
    private BigDecimal baseAccountAmount;

}
