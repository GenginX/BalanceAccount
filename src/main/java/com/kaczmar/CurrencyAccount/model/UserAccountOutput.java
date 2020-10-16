package com.kaczmar.CurrencyAccount.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UserAccountOutput {

    private String name;

    private String surname;

    private String pesel;

    private BigDecimal baseAccountAmount;

    private String currencyCode;


}
