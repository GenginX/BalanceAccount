package com.kaczmar.CurrencyAccount.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String surname;

    private String pesel;

    private BigDecimal currentAccountBalance;

    private String currencyCode;

    @OneToMany(mappedBy = "userMainAccount", cascade = CascadeType.ALL)
    private List<UserSubAccount> userSubAccounts = new ArrayList<>();

    public UserAccountOutput convertFromUserAccountToOutput() {
        return UserAccountOutput.builder()
                .name(this.name)
                .surname(this.surname)
                .pesel(this.pesel)
                .baseAccountAmount(this.currentAccountBalance)
                .currencyCode(this.currencyCode)
                .build();
    }

}
