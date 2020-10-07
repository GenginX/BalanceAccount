package com.kaczmar.CurrencyAccount.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RatesTable {

    private String no;

    private LocalDate effectiveDate;

    private BigDecimal mid;


}
