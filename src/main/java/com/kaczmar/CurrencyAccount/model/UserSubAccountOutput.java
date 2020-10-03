package com.kaczmar.CurrencyAccount.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserSubAccountOutput {

    private String pesel;

    private String currencyCode;

    private BigDecimal currentAccountBalance;


}
