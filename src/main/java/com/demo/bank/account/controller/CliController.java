package com.demo.bank.account.controller;

import com.demo.bank.account.service.AccountService;
import com.demo.bank.account.entity.Account;
import com.demo.bank.account.entity.Currency;
import com.demo.bank.conversion.service.CurrencyConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CliController {

    private final AccountService accountService;
    private final CurrencyConversionService currencyConversionService;

    @Autowired
    public CliController(AccountService accountService, CurrencyConversionService currencyConversionService) {
        this.accountService = accountService;
        this.currencyConversionService = currencyConversionService;
    }

    public void handleNewAccount(String[] parts) {
        if (parts.length != 4) {
            log.info("Usage: NewAccount [First Name] [Last Name] [Currency]");
            return;
        }
        String firstName = parts[1];
        String lastName = parts[2];
        String currencyStr = parts[3].toUpperCase();
        Currency currencyEnum;
        try {
            currencyEnum = Currency.valueOf(currencyStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid currency. Allowed: USD, EUR, RON");
            return;
        }
        Account newAcc = accountService.createAccount(firstName, lastName, currencyEnum);
        log.info("Account created. Account number: {}, Currency: {}", newAcc.getId(), newAcc.getCurrency());
    }

    public void handleDeposit(String[] parts) {
        if (parts.length != 4) {
            log.info("Usage: Deposit [Amount] [Currency] [Account number]");
            return;
        }
        String depositAmountStr = parts[1].replace(',', '.');
        double depositAmount;
        try {
            depositAmount = Double.parseDouble(depositAmountStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid deposit amount");
            return;
        }
        String currencyStr = parts[2].toUpperCase();
        Currency currencyEnum;
        try {
            currencyEnum = Currency.valueOf(currencyStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid currency. Allowed: USD, EUR, RON");
            return;
        }
        long accountId;
        try {
            accountId = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            log.warn("Invalid account number");
            return;
        }
        try {
            Account acc = accountService.deposit(accountId, depositAmount, currencyEnum);
            log.info("Deposit successful. Account: {}, New balance: {} {}", acc.getFullName(), acc.getBalance(), acc.getCurrency());
        } catch (Exception e) {
            log.warn("Deposit failed: {}", e.getMessage());
        }
    }

    public void handleWithdraw(String[] parts) {
        if (parts.length != 4) {
            log.info("Usage: Withdraw [Amount] [Currency] [Account number]");
            return;
        }
        String withdrawAmountStr = parts[1].replace(',', '.');
        double withdrawAmount;
        try {
            withdrawAmount = Double.parseDouble(withdrawAmountStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid withdraw amount");
            return;
        }
        String currencyStr = parts[2].toUpperCase();
        Currency currencyEnum;
        try {
            currencyEnum = Currency.valueOf(currencyStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid currency. Allowed: USD, EUR, RON");
            return;
        }
        long accountId;
        try {
            accountId = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            log.warn("Invalid account number");
            return;
        }
        try {
            Account acc = accountService.withdraw(accountId, withdrawAmount, currencyEnum);
            log.info("Withdraw successful. Account: {}, New balance: {} {}", acc.getFullName(), acc.getBalance(), acc.getCurrency());
        } catch (Exception e) {
            log.warn("Withdraw failed: {}", e.getMessage());
        }
    }

    public void handleBalance(String[] parts) {
        if (parts.length != 2) {
            log.info("Usage: Balance [Account number]");
            return;
        }

        Long balanceId = Long.parseLong(parts[1]);
        Account balAcc = accountService.getAccountById(balanceId);
        if (balAcc != null) {
            log.info("Account holder: {}, Balance: {} {}",
                       balAcc.getFullName(), balAcc.getBalance(), balAcc.getCurrency());
        } else {
            log.error("Account not found.");
        }
    }

    public void handleConvert(String[] parts) {
        if (parts.length != 4) {
            log.info("Usage: Convert [Amount] [Currency_1] [Currency_2]");
            return;
        }

        String convertAmountStr = parts[1].replace(',', '.');
        double convertAmount;
        try {
            convertAmount = Double.parseDouble(convertAmountStr);
        } catch (NumberFormatException e) {
            log.error("Invalid amount format. Use e.g. 5.0 or 5,0");
            return;
        }

        if (convertAmount <= 0) {
            log.warn("Amount must be positive.");
            return;
        }

        String fromCurrency = parts[2].toUpperCase();
        String toCurrency = parts[3].toUpperCase();
        try {
            double rate = currencyConversionService.convert(Currency.valueOf(fromCurrency), Currency.valueOf(toCurrency), 1.0);
            double converted = currencyConversionService.convert(Currency.valueOf(fromCurrency), Currency.valueOf(toCurrency), convertAmount);
            log.info("{} {} = {} {} (Rate: {})",
                       convertAmount, fromCurrency, converted, toCurrency, rate);
        } catch (Exception e) {
            log.error("Conversion failed: {}", e.getMessage());
        }
    }

    public void handleUnknownCommand() {
        log.info("Unknown command. Valid commands: NewAccount, Deposit, Withdraw, Balance, Convert, Quit");
    }
}
