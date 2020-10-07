package com.kaczmar.CurrencyAccount.dto;

import jdk.jfr.DataAmount;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CurrencyExchangeDto {

    @NotNull
    private String currencyFrom;

    @NotNull
    private String currencyTo;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    @DecimalMin(value = "0.00")
    private BigDecimal amount;
}
