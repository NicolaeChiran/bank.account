package com.demo.bank.account.service;

import com.demo.bank.account.entity.Account;
import com.demo.bank.account.entity.Currency;
import com.demo.bank.account.repository.AccountRepository;
import com.demo.bank.conversion.service.CurrencyConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CurrencyConversionService currencyConversionService;

    @Autowired
    public AccountService(AccountRepository accountRepository, CurrencyConversionService currencyConversionService) {
        this.accountRepository = accountRepository;
        this.currencyConversionService = currencyConversionService;
    }

    public Account createAccount(String firstName, String lastName, Currency currency) {
        Account account = new Account(firstName, lastName, currency);
        return accountRepository.save(account);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    // Unified deposit method with currency conversion
    public Account deposit(Long accountId, double amount, Currency currency) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        Account account = getAccountById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        Currency inputCurrency = currency;
        Currency accountCurrency = account.getCurrency();
        double finalAmount = amount;

        // Apply currency conversion if needed
        if (!inputCurrency.equals(accountCurrency)) {
            try {
                finalAmount = currencyConversionService.convert(inputCurrency, accountCurrency, amount);
                double conversionRate = finalAmount / amount;
                log.info("Converted {} {} to {} {} (rate: {})", amount, inputCurrency, finalAmount, accountCurrency, conversionRate);
            } catch (Exception e) {
                throw new RuntimeException("Currency conversion failed: " + e.getMessage());
            }
        }
        account.deposit(finalAmount);
        return accountRepository.save(account);
    }

    // Unified withdraw method with currency conversion
    public Account withdraw(Long accountId, double amount, Currency currency) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }

        Account account = getAccountById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        Currency inputCurrency = currency;
        Currency accountCurrency = account.getCurrency();
        double finalAmount = amount;

        // Apply currency conversion if needed
        if (!inputCurrency.equals(accountCurrency)) {
            try {
                finalAmount = currencyConversionService.convert(inputCurrency, accountCurrency, amount);
                double conversionRate = finalAmount / amount;
                log.info("Converted {} {} to {} {} (rate: {})", amount, inputCurrency, finalAmount, accountCurrency, conversionRate);
            } catch (Exception e) {
                throw new RuntimeException("Currency conversion failed: " + e.getMessage());
            }
        }
        boolean success = account.withdraw(finalAmount);
        if (!success) {
            throw new RuntimeException("Withdraw failed. Insufficient balance or invalid amount");
        }
        return accountRepository.save(account);
    }
}
