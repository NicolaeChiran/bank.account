package com.demo.bank.conversion.entity;

import com.demo.bank.account.entity.Currency;
import lombok.Getter;

@Getter
public class ConversionResponse {
    private final double amount;
    private final Currency from;
    private final Currency to;
    private final double rate;
    private final double convertedAmount;

    public ConversionResponse(double amount, Currency from, Currency to, double rate, double convertedAmount) {
        this.amount = amount;
        this.from = from;
        this.to = to;
        this.rate = rate;
        this.convertedAmount = convertedAmount;
    }
}
