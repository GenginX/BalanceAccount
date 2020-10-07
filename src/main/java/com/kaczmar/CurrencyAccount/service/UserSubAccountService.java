package com.kaczmar.CurrencyAccount.service;

import com.kaczmar.CurrencyAccount.dto.CreateUserSubAccountDto;
import com.kaczmar.CurrencyAccount.dto.CurrencyExchangeDto;
import com.kaczmar.CurrencyAccount.exceptions.AccountWithRemainingCurrencyNotExists;
import com.kaczmar.CurrencyAccount.exceptions.NotEnoughMoneyOnAccount;
import com.kaczmar.CurrencyAccount.model.*;
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
    private final String urlUSD = "http://api.nbp.pl/api/exchangerates/rates/a/usd/?format=json";

    private UserSubAccountRepository userSubAccountRepository;
    private UserAccountService userAccountService;
    private RestTemplateService restTemplateService;

    public UserSubAccountService(UserSubAccountRepository userSubAccountRepository,
                                 UserAccountService userAccountService,
                                 RestTemplateService restTemplateService) {
        this.userSubAccountRepository = userSubAccountRepository;
        this.userAccountService = userAccountService;
        this.restTemplateService = restTemplateService;
    }

    public UserSubAccount createUserSubAccount(CreateUserSubAccountDto userSubAccountDto) {
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


    public List<UserSubAccount> convertPlnToUsd(String pesel) throws IOException, AccountWithRemainingCurrencyNotExists, JSONException {
        final String actualValue = getApiResponse();
        BigDecimal actualValueConverted = new BigDecimal(actualValue);

        List<UserSubAccount> allSubAccountsByPesel = getAllSubAccountsByPesel(pesel);
        Optional<UserSubAccount> usdAccount = allSubAccountsByPesel.stream()
                .filter(e -> e.getCurrencyCode().equals(Currency.USD.toString()))
                .findAny();

        if (usdAccount.isEmpty()) {
        }

        UserSubAccount userUsdAccount = usdAccount.get();
        Optional<UserSubAccount> plnAccount = allSubAccountsByPesel.stream()
                .filter(e -> e.getCurrencyCode().equals(Currency.PLN.toString()))
                .findAny();

        if (plnAccount.isEmpty()) {

        }
        UserSubAccount userPlnAccount = plnAccount.get();
        BigDecimal usdBalance = userUsdAccount.getCurrentAccountBalance();
        BigDecimal plnBalance = userPlnAccount.getCurrentAccountBalance();

        System.out.println(usdBalance.toString());
        System.out.println(plnBalance.toString());
        System.out.println(actualValueConverted);

        BigDecimal newUsdBalance = usdBalance.add(plnBalance.divide(actualValueConverted, 2, RoundingMode.HALF_UP).setScale(2, RoundingMode.CEILING));
        BigDecimal newPLNBalance = plnBalance.subtract(plnBalance);

        userUsdAccount.setCurrentAccountBalance(newUsdBalance);
        userSubAccountRepository.save(userUsdAccount);
        userPlnAccount.setCurrentAccountBalance(newPLNBalance);
        userSubAccountRepository.save(userPlnAccount);
        return getAllSubAccountsByPesel(pesel);

    }

    private String getApiResponse() throws IOException, JSONException {
        JSONObject response = readJsonFromUrl(urlUSD);
        JSONArray ratesArray = response.getJSONArray("rates");
        JSONObject rates = ratesArray.getJSONObject(0);
        final String currentValue = rates.get("mid").toString();
        return currentValue;
    }

    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public String currencyCalculator(CurrencyExchangeDto dto) {
        BigDecimal providedMoney = dto.getAmount();
        BigDecimal result = getCorrectCalculation(providedMoney, dto.getCurrencyFrom(), dto.getCurrencyTo());

        return providedMoney + dto.getCurrencyFrom() + " to: " + result.toString() + dto.getCurrencyTo();

    }

    private BigDecimal getCorrectCalculation(BigDecimal money, String currencyFrom, String currencyTo) {
        BigDecimal actualCurrencyUSD;
        BigDecimal result;

        if (currencyFrom.toUpperCase().equals("PLN")) {
            actualCurrencyUSD = restTemplateService.getActualCurrency(currencyTo);
            result = money.divide(actualCurrencyUSD, 2, RoundingMode.UP);
        } else {
            actualCurrencyUSD = restTemplateService.getActualCurrency(currencyFrom);
            result = money.multiply(actualCurrencyUSD).setScale(2, RoundingMode.UP);
        }

        return result;
    }

    public List<UserSubAccountOutput> ExchangeCurrencyOnSubAccount(CurrencyExchangeDto dto, String pesel) throws AccountWithRemainingCurrencyNotExists, NotEnoughMoneyOnAccount {

        List<UserSubAccount> allSubAccountsByPesel = getAllSubAccountsByPesel(pesel);

        UserSubAccount currencyFromAccount = getCurrencyAccount(dto.getCurrencyFrom(), allSubAccountsByPesel);
        UserSubAccount currencyToAccount = getCurrencyAccount(dto.getCurrencyTo(), allSubAccountsByPesel);
        if(!isEnoughMoneyOnAccount(currencyFromAccount, dto.getAmount())){
            throw new NotEnoughMoneyOnAccount(NOT_ENOUGH_MONEY);
        }
        BigDecimal convertedAmount = getCorrectCalculation(dto.getAmount(), dto.getCurrencyFrom(), dto.getCurrencyTo());
        BigDecimal newAmountForCurrencyFromAccount = currencyFromAccount.getCurrentAccountBalance().subtract(dto.getAmount());
        BigDecimal newAmountForCurrencyToAccount = currencyToAccount.getCurrentAccountBalance().add(convertedAmount);
        updateMoneyOnAccount(currencyFromAccount,newAmountForCurrencyFromAccount);
        updateMoneyOnAccount(currencyToAccount, newAmountForCurrencyToAccount);

        return getAllSubAccountsByPesel(pesel).stream()
                .map(e-> e.convertFromUserSubAccountToOutput())
                .collect(Collectors.toList());
    }

    private UserSubAccount getCurrencyAccount(String code, List<UserSubAccount> subAccountList) throws AccountWithRemainingCurrencyNotExists {
        Optional<UserSubAccount> any = subAccountList.stream()
                .filter(e -> e.getCurrencyCode().equalsIgnoreCase(code))
                .findAny();
        if(any.isEmpty()){
            throw new AccountWithRemainingCurrencyNotExists(NO_CURRENCY + " " + code);
        }
        return any.get();
    }

    private boolean isEnoughMoneyOnAccount(UserSubAccount account, BigDecimal amount){
        BigDecimal currentAccountBalance = account.getCurrentAccountBalance();
        return currentAccountBalance.subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
    }

    private UserSubAccount updateMoneyOnAccount(UserSubAccount account, BigDecimal money){
        account.setCurrentAccountBalance(money);
        return userSubAccountRepository.save(account);
    }


}
