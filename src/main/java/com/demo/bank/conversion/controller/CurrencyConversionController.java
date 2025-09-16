package com.demo.bank.conversion.controller;

import com.demo.bank.account.entity.Currency;
import com.demo.bank.conversion.service.CurrencyConversionService;
import com.demo.bank.conversion.entity.ConversionResponse;
import com.demo.bank.utils.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "Currency Conversion API", description = "Convert between currencies using Frankfurter API")
@RestController
@RequestMapping("/api/currency")
@ApiResponses(
   value={
       @ApiResponse(
           responseCode = "400",
           description = "Bad Request",
           content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.class, hidden = true))),
       @ApiResponse(
           responseCode = "500",
           description = "Bad Request",
           content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.class, hidden = true)))
   })
public class CurrencyConversionController {
    private final CurrencyConversionService currencyConversionService;

    public CurrencyConversionController(CurrencyConversionService currencyConversionService) {
        this.currencyConversionService = currencyConversionService;
    }

    @Operation(summary = "Convert amount from one currency to another")
    @GetMapping("/convert")
    public ConversionResponse convert(
        @Parameter(description = "Amount to convert") @RequestParam double amount,
        @Parameter(description = "Source currency code") @RequestParam Currency from,
        @Parameter(description = "Target currency code") @RequestParam Currency to) {
        double converted = currencyConversionService.convert(from, to, amount);
        double rate = converted / amount;
        return new ConversionResponse(amount, from, to, rate, converted);
    }
}
