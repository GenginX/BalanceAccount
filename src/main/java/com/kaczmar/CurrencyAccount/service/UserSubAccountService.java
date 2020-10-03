package com.kaczmar.CurrencyAccount.service;

import com.google.gson.Gson;
import com.kaczmar.CurrencyAccount.dto.CreateUserSubAccountDto;
import com.kaczmar.CurrencyAccount.exceptions.AccountWithRemainingCurrencyNotExists;
import com.kaczmar.CurrencyAccount.model.*;
import com.kaczmar.CurrencyAccount.repository.UserSubAccountRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

@Service
public class UserSubAccountService {

    public static final String NO_USD = "THIS USER DON'T HAVE USD ACCOUNT";
    public static final String NO_PLN = "THIS USER DON'T HAVE PLN ACCOUNT";
    private final String urlUSD = "http://api.nbp.pl/api/exchangerates/rates/a/usd/?format=json";

    private UserSubAccountRepository userSubAccountRepository;
    private UserAccountService userAccountService;

    public UserSubAccountService(UserSubAccountRepository userSubAccountRepository, UserAccountService userAccountService) {
        this.userSubAccountRepository = userSubAccountRepository;
        this.userAccountService = userAccountService;
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
//        UserAccount user = userAccountService.getUserByPesel(pesel);
//        List<UserSubAccount> allByPesel = userSubAccountRepository.findAllByUserMainAccount(user.getId());
        return allByPesel;
    }


    public List<UserSubAccount> convertPlnToUsd(String pesel) throws IOException, AccountWithRemainingCurrencyNotExists, JSONException {
//        ApiResponse apiResponse = getApiResponse();
//        RatesTable rates = apiResponse.getRates();
        final String actualValue = getApiResponse();
        BigDecimal actualValueConverted = new BigDecimal(actualValue);

        List<UserSubAccount> allSubAccountsByPesel = getAllSubAccountsByPesel(pesel);
        Optional<UserSubAccount> usdAccount = allSubAccountsByPesel.stream()
                .filter(e -> e.getCurrencyCode().equals(Currency.USD.toString()))
                .findAny();

        if (usdAccount.isEmpty()) {
            throw new AccountWithRemainingCurrencyNotExists(NO_USD);
        }

        UserSubAccount userUsdAccount = usdAccount.get();
        Optional<UserSubAccount> plnAccount = allSubAccountsByPesel.stream()
                .filter(e -> e.getCurrencyCode().equals(Currency.PLN.toString()))
                .findAny();

        if (plnAccount.isEmpty()) {
            throw new AccountWithRemainingCurrencyNotExists(NO_PLN);

        }
        UserSubAccount userPlnAccount = plnAccount.get();
        BigDecimal usdBalance = userUsdAccount.getCurrentAccountBalance();
        BigDecimal plnBalance = userPlnAccount.getCurrentAccountBalance();

        System.out.println(usdBalance.toString());
        System.out.println(plnBalance.toString());
        System.out.println(actualValueConverted);

        BigDecimal newUsdBalance = usdBalance.add(plnBalance.divide(actualValueConverted,2, RoundingMode.HALF_UP).setScale(2, RoundingMode.CEILING));
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

}