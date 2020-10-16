package com.kaczmar.CurrencyAccount.service;

import com.kaczmar.CurrencyAccount.dto.CreateUserSubAccountDto;
import com.kaczmar.CurrencyAccount.dto.CurrencyExchangeDto;
import com.kaczmar.CurrencyAccount.exceptions.AccountWithRemainingCurrencyNotExists;
import com.kaczmar.CurrencyAccount.exceptions.NoUserFoundForGivenPeselException;
import com.kaczmar.CurrencyAccount.exceptions.NotEnoughMoneyOnAccount;
import com.kaczmar.CurrencyAccount.model.*;
import com.kaczmar.CurrencyAccount.repository.UserAccountRepository;
import com.kaczmar.CurrencyAccount.repository.UserSubAccountRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserSubAccountService {

    public static final String NO_CURRENCY = "THIS USER DON'T HAVE";
    public static final String NOT_ENOUGH_MONEY = "USER DON'T HAVE ENOUGH MONEY ON ACCOUNT TO DO THIS OPERATION";

    private final UserSubAccountRepository userSubAccountRepository;
    private final UserAccountService userAccountService;
    private final RestTemplateService restTemplateService;
    private final UserAccountRepository userAccountRepository;

    public UserSubAccountService(UserSubAccountRepository userSubAccountRepository,
                                 UserAccountService userAccountService,
                                 RestTemplateService restTemplateService,
                                 UserAccountRepository userAccountRepository) {
        this.userSubAccountRepository = userSubAccountRepository;
        this.userAccountService = userAccountService;
        this.restTemplateService = restTemplateService;
        this.userAccountRepository = userAccountRepository;
    }

    public UserSubAccount createUserSubAccount(CreateUserSubAccountDto userSubAccountDto) throws NoUserFoundForGivenPeselException {
        String userPesel = userSubAccountDto.getPesel();
        UserAccount userAccount = userAccountService.getUserByPesel(userPesel);

        UserSubAccount userSubAccount = UserSubAccount.builder()
                .currencyCode(userSubAccountDto.getCurrency())
                .currentAccountBalance(userSubAccountDto.getBaseAccountAmount())
                .userMainAccount(userAccount)
                .pesel(userPesel)
                .build();
        return userSubAccountRepository.save(userSubAccount);
    }


    public List<UserSubAccount> getAllSubAccountsByPesel(String pesel) {
        List<UserSubAccount> allByPesel = userSubAccountRepository.findAllByPesel(pesel);
        return allByPesel;
    }

    public void ExchangeCurrencyOnAccounts(CurrencyExchangeDto dto, String pesel) throws AccountWithRemainingCurrencyNotExists, NotEnoughMoneyOnAccount, NoUserFoundForGivenPeselException {
        if (isCurrencyInPLN(dto)) {
            ExchangeCurrencyBetweenMainAccountAndSubAccount(dto, pesel);
        }else{
            ExchangeCurrencyBetweenSubAccounts(dto, pesel);

        }
    }

    private void ExchangeCurrencyBetweenSubAccounts(CurrencyExchangeDto dto, String pesel) throws AccountWithRemainingCurrencyNotExists, NotEnoughMoneyOnAccount {
        List<UserSubAccount> allSubAccountsByPesel = getAllSubAccountsByPesel(pesel);

        UserSubAccount currencyFromAccount = getCurrencyAccount(dto.getCurrencyFrom(), allSubAccountsByPesel);
        UserSubAccount currencyToAccount = getCurrencyAccount(dto.getCurrencyTo(), allSubAccountsByPesel);
        if (!isEnoughMoneyOnAccount(currencyFromAccount, dto.getAmount())) {
            throw new NotEnoughMoneyOnAccount(NOT_ENOUGH_MONEY);
        }

        UpdateAmountOnAccounts(dto, currencyFromAccount, currencyToAccount);
    }

    private void UpdateAmountOnAccounts(CurrencyExchangeDto dto, UserSubAccount currencyFromAccount, UserSubAccount currencyToAccount) {
        BigDecimal convertedAmount = getCorrectCalculation(dto.getAmount(), dto.getCurrencyFrom(), dto.getCurrencyTo());
        BigDecimal newAmountForCurrencyFromAccount = currencyFromAccount.getCurrentAccountBalance().subtract(dto.getAmount());
        BigDecimal newAmountForCurrencyToAccount = currencyToAccount.getCurrentAccountBalance().add(convertedAmount);

        updateMoneyOnAccount(currencyFromAccount, newAmountForCurrencyFromAccount);
        updateMoneyOnAccount(currencyToAccount, newAmountForCurrencyToAccount);
    }

    private void UpdateAmountOnAccounts(CurrencyExchangeDto dto, UserAccount currencyFromAccount, UserSubAccount currencyToAccount) {
        BigDecimal convertedAmount = getCorrectCalculation(dto.getAmount(), dto.getCurrencyFrom(), dto.getCurrencyTo());
        BigDecimal newAmountForCurrencyFromAccount = currencyFromAccount.getCurrentAccountBalance().subtract(dto.getAmount());
        BigDecimal newAmountForCurrencyToAccount = currencyToAccount.getCurrentAccountBalance().add(convertedAmount);

        updateMoneyOnAccount(currencyFromAccount, newAmountForCurrencyFromAccount);
        updateMoneyOnAccount(currencyToAccount, newAmountForCurrencyToAccount);
    }

    private void UpdateAmountOnAccounts(CurrencyExchangeDto dto, UserSubAccount currencyFromAccount, UserAccount currencyToAccount) {
        BigDecimal convertedAmount = getCorrectCalculation(dto.getAmount(), dto.getCurrencyFrom(), dto.getCurrencyTo());
        BigDecimal newAmountForCurrencyFromAccount = currencyFromAccount.getCurrentAccountBalance().subtract(dto.getAmount());
        BigDecimal newAmountForCurrencyToAccount = currencyToAccount.getCurrentAccountBalance().add(convertedAmount);

        updateMoneyOnAccount(currencyFromAccount, newAmountForCurrencyFromAccount);
        updateMoneyOnAccount(currencyToAccount, newAmountForCurrencyToAccount);
    }

    private boolean isCurrencyInPLN(CurrencyExchangeDto dto) {
        if (dto.getCurrencyFrom().toUpperCase().equals("PLN") || dto.getCurrencyTo().toUpperCase().equals("PLN")) {
            return true;
        }
        return false;
    }

    private void ExchangeCurrencyBetweenMainAccountAndSubAccount(CurrencyExchangeDto dto, String pesel) throws NoUserFoundForGivenPeselException, AccountWithRemainingCurrencyNotExists, NotEnoughMoneyOnAccount {
        List<UserSubAccount> allSubAccountsByPesel = getAllSubAccountsByPesel(pesel);
        if (dto.getCurrencyTo().toUpperCase().equals("PLN")) {
            UserAccount currencyToAccount = userAccountService.getUserByPesel(pesel);
            UserSubAccount currencyFromAccount = getCurrencyAccount(dto.getCurrencyFrom(), allSubAccountsByPesel);
            if (!isEnoughMoneyOnAccount(currencyFromAccount, dto.getAmount())) {
                throw new NotEnoughMoneyOnAccount(NOT_ENOUGH_MONEY);
            }
            UpdateAmountOnAccounts(dto, currencyFromAccount, currencyToAccount);
        } else {
            UserAccount currencyFromAccount = userAccountService.getUserByPesel(pesel);
            UserSubAccount currencyToAccount = getCurrencyAccount(dto.getCurrencyTo(), allSubAccountsByPesel);
            if (!isEnoughMoneyOnAccount(currencyFromAccount, dto.getAmount())) {
                throw new NotEnoughMoneyOnAccount(NOT_ENOUGH_MONEY);
            }
            UpdateAmountOnAccounts(dto, currencyFromAccount, currencyToAccount);
        }
    }

    private UserSubAccount getCurrencyAccount(String code, List<UserSubAccount> subAccountList) throws AccountWithRemainingCurrencyNotExists {
        Optional<UserSubAccount> any = subAccountList.stream()
                .filter(e -> e.getCurrencyCode().equalsIgnoreCase(code))
                .findAny();
        if (any.isEmpty()) {
            throw new AccountWithRemainingCurrencyNotExists(NO_CURRENCY + " " + code);
        }
        return any.get();
    }

    private boolean isEnoughMoneyOnAccount(UserSubAccount account, BigDecimal amount) {
        BigDecimal currentAccountBalance = account.getCurrentAccountBalance();
        return currentAccountBalance.subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
    }

    private boolean isEnoughMoneyOnAccount(UserAccount account, BigDecimal amount) {
        BigDecimal currentAccountBalance = account.getCurrentAccountBalance();
        return currentAccountBalance.subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
    }

    private BigDecimal getCorrectCalculation(BigDecimal money, String currencyFrom, String currencyTo) {

        if (currencyFrom.toUpperCase().equals("PLN") || currencyTo.toUpperCase().equals("PLN")) {
            return convertFromOrToPLN(money, currencyFrom, currencyTo);
        } else {
            return convertBetweenNonPLNCurrencies(money, currencyFrom, currencyTo);
        }
    }

    private BigDecimal convertFromOrToPLN(BigDecimal money, String currencyFrom, String currencyTo) {

        if (currencyFrom.toUpperCase().equals("PLN")) {
            BigDecimal actualCurrencyUSD = restTemplateService.getActualCurrency(currencyTo);
            return money.divide(actualCurrencyUSD, 2, RoundingMode.UP);
        } else {
            BigDecimal actualCurrencyUSD = restTemplateService.getActualCurrency(currencyFrom);
            return money.multiply(actualCurrencyUSD).setScale(2, RoundingMode.UP);
        }

    }

    private BigDecimal convertBetweenNonPLNCurrencies(BigDecimal money, String currencyFrom, String currencyTo) {
        BigDecimal currencyFrom_ToPlnValue = restTemplateService.getActualCurrency(currencyFrom.toUpperCase());
        BigDecimal currencyTo_FromPlnValue = restTemplateService.getActualCurrency(currencyTo.toUpperCase());

        return currencyFrom_ToPlnValue.multiply(money).divide(currencyTo_FromPlnValue, 2, RoundingMode.UP);
    }

    private void updateMoneyOnAccount(UserSubAccount account, BigDecimal money) {
        account.setCurrentAccountBalance(money);
        userSubAccountRepository.save(account);
    }

    private void updateMoneyOnAccount(UserAccount account, BigDecimal money) {
        account.setCurrentAccountBalance(money);
        userAccountRepository.save(account);
    }


}
