package com.kaczmar.CurrencyAccount.service;

import com.kaczmar.CurrencyAccount.model.RateResponse;
import com.kaczmar.CurrencyAccount.model.RatesTable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

@Service
public class RestTemplateService {

    private final String apiURL = "http://api.nbp.pl/api/exchangerates/rates/a/{0}/?format=json";

    public BigDecimal getActualCurrency(String code){
        RestTemplate restTemplate = new RestTemplate();

        String apiUrlWithCode = MessageFormat.format(apiURL, code);
        RateResponse response = restTemplate.getForObject(
                apiUrlWithCode, RateResponse.class);
        List<RatesTable> rates = response.getRates();
        BigDecimal actualCurrency = rates.get(0).getMid();

        return actualCurrency;
    }

}
