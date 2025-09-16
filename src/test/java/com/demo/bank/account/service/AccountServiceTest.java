package com.demo.bank.account.service;

import com.demo.bank.account.entity.Account;
import com.demo.bank.account.entity.Currency;
import com.demo.bank.account.repository.AccountRepository;
import com.demo.bank.conversion.service.CurrencyConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CurrencyConversionService currencyConversionService;
    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAccount() {
        Account account = new Account("John", "Doe", Currency.USD);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        Account result = accountService.createAccount("John", "Doe", Currency.USD);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(Currency.USD, result.getCurrency());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testGetAccountById_found() {
        Account account = new Account("Jane", "Smith", Currency.EUR);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Account result = accountService.getAccountById(1L);
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void testGetAccountById_notFound() {
        when(accountRepository.findById(2L)).thenReturn(Optional.empty());
        Account result = accountService.getAccountById(2L);
        assertNull(result);
    }

    @Test
    void testDeposit_sameCurrency() {
        Account account = new Account("John", "Doe", Currency.USD);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        Account result = accountService.deposit(1L, 100.0, Currency.USD);
        assertEquals(100.0, result.getBalance());
        verify(accountRepository).save(account);
        verify(currencyConversionService, never()).convert(any(), any(), anyDouble());
    }

    @Test
    void testDeposit_differentCurrency() {
        Account account = new Account("John", "Doe", Currency.USD);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(currencyConversionService.convert(Currency.EUR, Currency.USD, 100.0)).thenReturn(110.0);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        Account result = accountService.deposit(1L, 100.0, Currency.EUR);
        assertEquals(110.0, result.getBalance());
        verify(currencyConversionService).convert(Currency.EUR, Currency.USD, 100.0);
        verify(accountRepository).save(account);
    }

    @Test
    void testDeposit_negativeAmount() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            accountService.deposit(1L, -50.0, Currency.USD)
        );
        assertEquals("Deposit amount must be positive", ex.getMessage());
    }

    @Test
    void testDeposit_accountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            accountService.deposit(99L, 100.0, Currency.USD)
        );
        assertEquals("Account not found", ex.getMessage());
    }

    @Test
    void testDeposit_conversionFails() {
        Account account = new Account("John", "Doe", Currency.USD);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(currencyConversionService.convert(Currency.EUR, Currency.USD, 100.0)).thenThrow(new RuntimeException("Conversion error"));
        Exception ex = assertThrows(RuntimeException.class, () ->
            accountService.deposit(1L, 100.0, Currency.EUR)
        );
        assertTrue(ex.getMessage().contains("Currency conversion failed"));
    }

    @Test
    void testWithdraw_sameCurrency_success() {
        Account account = new Account("John", "Doe", Currency.USD);
        account.deposit(200.0);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        Account result = accountService.withdraw(1L, 50.0, Currency.USD);
        assertEquals(150.0, result.getBalance());
        verify(accountRepository).save(account);
        verify(currencyConversionService, never()).convert(any(), any(), anyDouble());
    }

    @Test
    void testWithdraw_differentCurrency_success() {
        Account account = new Account("John", "Doe", Currency.USD);
        account.deposit(200.0);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(currencyConversionService.convert(Currency.EUR, Currency.USD, 50.0)).thenReturn(60.0);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        Account result = accountService.withdraw(1L, 50.0, Currency.EUR);
        assertEquals(140.0, result.getBalance());
        verify(currencyConversionService).convert(Currency.EUR, Currency.USD, 50.0);
        verify(accountRepository).save(account);
    }

    @Test
    void testWithdraw_negativeAmount() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            accountService.withdraw(1L, -10.0, Currency.USD)
        );
        assertEquals("Withdraw amount must be positive", ex.getMessage());
    }

    @Test
    void testWithdraw_accountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            accountService.withdraw(99L, 50.0, Currency.USD)
        );
        assertEquals("Account not found", ex.getMessage());
    }

    @Test
    void testWithdraw_insufficientBalance() {
        Account account = new Account("John", "Doe", Currency.USD);
        account.deposit(30.0);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Exception ex = assertThrows(RuntimeException.class, () ->
            accountService.withdraw(1L, 50.0, Currency.USD)
        );
        assertEquals("Withdraw failed. Insufficient balance or invalid amount", ex.getMessage());
    }

    @Test
    void testWithdraw_conversionFails() {
        Account account = new Account("John", "Doe", Currency.USD);
        account.deposit(100.0);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(currencyConversionService.convert(Currency.EUR, Currency.USD, 50.0)).thenThrow(new RuntimeException("Conversion error"));
        Exception ex = assertThrows(RuntimeException.class, () ->
            accountService.withdraw(1L, 50.0, Currency.EUR)
        );
        assertTrue(ex.getMessage().contains("Currency conversion failed"));
    }
}
