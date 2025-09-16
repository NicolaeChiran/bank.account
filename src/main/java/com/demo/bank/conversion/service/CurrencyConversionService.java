package com.demo.bank.conversion.service;

import com.demo.bank.account.entity.Currency;
import com.demo.bank.conversion.repository.CurrencyConversionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyConversionService {
    private final CurrencyConversionRepository currencyConversionRepository;

    @Autowired
    public CurrencyConversionService(CurrencyConversionRepository currencyConversionRepository) {
        this.currencyConversionRepository = currencyConversionRepository;
    }

    public double convert(Currency from, Currency to, double amount) {
        double rate = currencyConversionRepository.getConversionRate(from, to);
        return amount * rate;
    }
}

