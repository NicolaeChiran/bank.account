package com.demo.bank.account.controller;

import com.demo.bank.account.service.AccountService;
import com.demo.bank.account.entity.Account;
import com.demo.bank.account.entity.Currency;
import com.demo.bank.utils.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@Slf4j
@Tag(name = "Bank Account API", description = "Operations for bank accounts")
@RestController
@RequestMapping("/api/bank")
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
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Create a new bank account")
    @PostMapping("/account")
    public ResponseEntity<?> createAccount(
        @Parameter(description = "First name of account holder") @RequestParam String firstName,
        @Parameter(description = "Last name of account holder") @RequestParam String lastName,
        @Parameter(description = "Currency for the account") @RequestParam Currency currency) {
        try {
            Account account = accountService.createAccount(firstName, lastName, currency);
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid currency: " + currency);
        }
    }

    @Operation(summary = "Deposit money into an account")
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
        @Parameter(description = "Account number") @RequestParam Long accountId,
        @Parameter(description = "Amount to deposit") @RequestParam double amount,
        @Parameter(description = "Currency of the amount") @RequestParam Currency currency) {
        try {
            Account account = accountService.deposit(accountId, amount, currency);
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid currency: " + currency);
        }
    }

    @Operation(summary = "Withdraw money from an account")
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
        @Parameter(description = "Account number") @RequestParam Long accountId,
        @Parameter(description = "Amount to withdraw") @RequestParam double amount,
        @Parameter(description = "Currency of the amount") @RequestParam Currency currency) {
        try {
            Account account = accountService.withdraw(accountId, amount, currency);
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid currency: " + currency);
        }
    }

    @Operation(summary = "Get account balance and details")
    @GetMapping("/balance")
    public Account getBalance(
        @Parameter(description = "Account number") @RequestParam Long accountId) {
        return accountService.getAccountById(accountId);
    }
}
