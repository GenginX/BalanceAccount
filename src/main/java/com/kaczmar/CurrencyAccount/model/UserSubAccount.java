package com.kaczmar.CurrencyAccount.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Currency;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String pesel;

    private String currencyCode;

    private BigDecimal currentAccountBalance;

    @ManyToOne
    @JoinColumn(name = "pesel.useraccount", nullable = false)
    private UserAccount userMainAccount;


    public UserSubAccountOutput convertFromUserSubAccountToOutput(){
        return UserSubAccountOutput.builder()
                .pesel(this.pesel)
                .currencyCode(this.currencyCode)
                .currentAccountBalance(this.currentAccountBalance)
                .build();
    }
}
