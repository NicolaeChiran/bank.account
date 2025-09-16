package com.demo.bank.account.controller;

import com.demo.bank.account.entity.Account;
import com.demo.bank.account.entity.Currency;
import com.demo.bank.account.service.AccountService;
import com.demo.bank.conversion.service.CurrencyConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.mockito.Mockito.*;

class CliControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private CurrencyConversionService currencyConversionService;

    @InjectMocks
    private CliController cliController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleNewAccount_validInput() {
        Account mockAccount = mock(Account.class);
        when(accountService.createAccount("John", "Doe", Currency.USD)).thenReturn(mockAccount);
        when(mockAccount.getId()).thenReturn(1L);
        when(mockAccount.getCurrency()).thenReturn(Currency.USD);

        cliController.handleNewAccount(new String[]{"NewAccount", "John", "Doe", "USD"});

        verify(accountService).createAccount("John", "Doe", Currency.USD);
    }

    @Test
    void testHandleNewAccount_invalidCurrency() {
        cliController.handleNewAccount(new String[]{"NewAccount", "John", "Doe", "GBP"});
        verify(accountService, never()).createAccount(anyString(), anyString(), any(Currency.class));
    }

    @Test
    void testHandleDeposit_validInput() {
        Account mockAccount = mock(Account.class);
        when(accountService.deposit(1L, 100.0, Currency.USD)).thenReturn(mockAccount);
        when(mockAccount.getFullName()).thenReturn("John Doe");
        when(mockAccount.getBalance()).thenReturn(200.0);
        when(mockAccount.getCurrency()).thenReturn(Currency.USD);

        cliController.handleDeposit(new String[]{"Deposit", "100", "USD", "1"});

        verify(accountService).deposit(1L, 100.0, Currency.USD);
    }

    @Test
    void testHandleDeposit_invalidAmount() {
        cliController.handleDeposit(new String[]{"Deposit", "abc", "USD", "1"});
        verify(accountService, never()).deposit(anyLong(), anyDouble(), any(Currency.class));
    }

    @Test
    void testHandleWithdraw_validInput() {
        Account mockAccount = mock(Account.class);
        when(accountService.withdraw(1L, 50.0, Currency.USD)).thenReturn(mockAccount);
        when(mockAccount.getFullName()).thenReturn("John Doe");
        when(mockAccount.getBalance()).thenReturn(150.0);
        when(mockAccount.getCurrency()).thenReturn(Currency.USD);

        cliController.handleWithdraw(new String[]{"Withdraw", "50", "USD", "1"});

        verify(accountService).withdraw(1L, 50.0, Currency.USD);
    }

    @Test
    void testHandleBalance_validInput() {
        Account mockAccount = mock(Account.class);
        when(accountService.getAccountById(1L)).thenReturn(mockAccount);
        when(mockAccount.getFullName()).thenReturn("John Doe");
        when(mockAccount.getBalance()).thenReturn(150.0);
        when(mockAccount.getCurrency()).thenReturn(Currency.USD);

        cliController.handleBalance(new String[]{"Balance", "1"});

        verify(accountService).getAccountById(1L);
    }

    @Test
    void testHandleConvert_validInput() {
        when(currencyConversionService.convert(Currency.USD, Currency.EUR, 1.0)).thenReturn(0.9);
        when(currencyConversionService.convert(Currency.USD, Currency.EUR, 100.0)).thenReturn(90.0);

        cliController.handleConvert(new String[]{"Convert", "100", "USD", "EUR"});

        verify(currencyConversionService).convert(Currency.USD, Currency.EUR, 1.0);
        verify(currencyConversionService).convert(Currency.USD, Currency.EUR, 100.0);
    }

    @Test
    void testHandleUnknownCommand() {
        cliController.handleUnknownCommand();
        // No service interaction, just log output
    }
}
