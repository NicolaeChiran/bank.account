package com.demo.bank.account.controller;

import com.demo.bank.account.entity.Account;
import com.demo.bank.account.entity.Currency;
import com.demo.bank.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import com.demo.bank.utils.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    // Utility to set id on Account
    private void setAccountId(Account account, Long id) {
        try {
            Field idField = Account.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(account, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setup() {
        AccountController controller = new AccountController(accountService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        // Given
        Account mockAccount = new Account("John", "Doe", Currency.USD);
        setAccountId(mockAccount, 1L);

        when(accountService.createAccount("John", "Doe", Currency.USD)).thenReturn(mockAccount);

        // When & Then
        mockMvc.perform(post("/api/bank/account")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(0.0));

        verify(accountService).createAccount("John", "Doe", Currency.USD);
    }

    @Test
    void testDeposit_Success_SameCurrency() throws Exception {
        // Given
        Account mockAccount = new Account("John", "Doe", Currency.USD);
        mockAccount.deposit(100.0);
        setAccountId(mockAccount, 1L);

        when(accountService.deposit(1L, 100.0, Currency.USD)).thenReturn(mockAccount);

        // When & Then
        mockMvc.perform(post("/api/bank/deposit")
                .param("accountId", "1")
                .param("amount", "100.0")
                .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(100.0));

        verify(accountService).deposit(1L, 100.0, Currency.USD);
    }

    @Test
    void testDeposit_Success_DifferentCurrency() throws Exception {
        // Given - depositing EUR into USD account with conversion
        Account mockAccount = new Account("John", "Doe", Currency.USD);
        mockAccount.deposit(118.0); // 100 EUR converted to 118 USD
        setAccountId(mockAccount, 1L);

        when(accountService.deposit(1L, 100.0, Currency.EUR)).thenReturn(mockAccount);

        // When & Then
        mockMvc.perform(post("/api/bank/deposit")
                .param("accountId", "1")
                .param("amount", "100.0")
                .param("currency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(118.0))
                .andExpect(jsonPath("$.currency").value("USD"));

        verify(accountService).deposit(1L, 100.0, Currency.EUR);
    }

    @Test
    void testDeposit_InvalidAmount_Negative() throws Exception {
        // Given
        when(accountService.deposit(anyLong(), anyDouble(), any(Currency.class)))
                .thenThrow(new IllegalArgumentException("Deposit amount must be positive"));

        // When & Then
        mockMvc.perform(post("/api/bank/deposit")
                .param("accountId", "1")
                .param("amount", "-50.0")
                .param("currency", "USD"))
                .andExpect(status().isBadRequest());

        verify(accountService).deposit(1L, -50.0, Currency.USD);
    }

    @Test
    void testDeposit_AccountNotFound() throws Exception {
        // Given
        when(accountService.deposit(999L, 100.0, Currency.USD))
                .thenThrow(new IllegalArgumentException("Account not found"));

        // When & Then
        mockMvc.perform(post("/api/bank/deposit")
                .param("accountId", "999")
                .param("amount", "100.0")
                .param("currency", "USD"))
                .andExpect(status().isBadRequest());

        verify(accountService).deposit(999L, 100.0, Currency.USD);
    }

    @Test
    void testWithdraw_Success_SameCurrency() throws Exception {
        // Given
        Account mockAccount = new Account("John", "Doe", Currency.USD);
        mockAccount.deposit(200.0);
        mockAccount.withdraw(50.0);
        setAccountId(mockAccount, 1L);

        when(accountService.withdraw(1L, 50.0, Currency.USD)).thenReturn(mockAccount);

        // When & Then
        mockMvc.perform(post("/api/bank/withdraw")
                .param("accountId", "1")
                .param("amount", "50.0")
                .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(150.0))
                .andExpect(jsonPath("$.currency").value("USD"));

        verify(accountService).withdraw(1L, 50.0, Currency.USD);
    }

    @Test
    void testWithdraw_Success_DifferentCurrency() throws Exception {
        // Given - withdrawing EUR from USD account with conversion
        Account mockAccount = new Account("John", "Doe", Currency.USD);
        mockAccount.deposit(200.0);
        mockAccount.withdraw(59.0); // 50 EUR converted to 59 USD
        setAccountId(mockAccount, 1L);

        when(accountService.withdraw(1L, 50.0, Currency.EUR)).thenReturn(mockAccount);

        // When & Then
        mockMvc.perform(post("/api/bank/withdraw")
                .param("accountId", "1")
                .param("amount", "50.0")
                .param("currency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(141.0));

        verify(accountService).withdraw(1L, 50.0, Currency.EUR);
    }

    @Test
    void testWithdraw_InvalidAmount_Negative() throws Exception {
        // Given
        when(accountService.withdraw(anyLong(), anyDouble(), any(Currency.class)))
                .thenThrow(new IllegalArgumentException("Withdraw amount must be positive"));

        // When & Then
        mockMvc.perform(post("/api/bank/withdraw")
                .param("accountId", "1")
                .param("amount", "-25.0")
                .param("currency", "USD"))
                .andExpect(status().isBadRequest());

        verify(accountService).withdraw(1L, -25.0, Currency.USD);
    }

    @Test
    void testWithdraw_AccountNotFound() throws Exception {
        // Given
        when(accountService.withdraw(999L, 50.0, Currency.USD))
                .thenThrow(new IllegalArgumentException("Account not found"));

        // When & Then
        mockMvc.perform(post("/api/bank/withdraw")
                .param("accountId", "999")
                .param("amount", "50.0")
                .param("currency", "USD"))
                .andExpect(status().isBadRequest());

        verify(accountService).withdraw(999L, 50.0, Currency.USD);
    }

    @Test
    void testGetBalance_Success() throws Exception {
        // Given
        Account mockAccount = new Account("John", "Doe", Currency.USD);
        mockAccount.deposit(250.0);
        setAccountId(mockAccount, 1L);

        when(accountService.getAccountById(1L)).thenReturn(mockAccount);

        // When & Then
        mockMvc.perform(get("/api/bank/balance")
                .param("accountId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(250.0));

        verify(accountService).getAccountById(1L);
    }

    @Test
    void testGetBalance_AccountNotFound() throws Exception {
        // Given
        when(accountService.getAccountById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/bank/balance")
                .param("accountId", "999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(accountService).getAccountById(999L);
    }

    @Test
    void testCreateAccount_MissingParameters() throws Exception {
        // When & Then - Missing lastName parameter
        mockMvc.perform(post("/api/bank/account")
                .param("firstName", "John")
                .param("currency", "USD"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    @Test
    void testDeposit_MissingParameters() throws Exception {
        // When & Then - Missing currency parameter
        mockMvc.perform(post("/api/bank/deposit")
                .param("accountId", "1")
                .param("amount", "100.0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    @Test
    void testWithdraw_MissingParameters() throws Exception {
        // When & Then - Missing accountId parameter
        mockMvc.perform(post("/api/bank/withdraw")
                .param("amount", "50.0")
                .param("currency", "USD"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    @Test
    void testGetBalance_MissingParameters() throws Exception {
        // When & Then - Missing accountId parameter
        mockMvc.perform(get("/api/bank/balance"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }
}
