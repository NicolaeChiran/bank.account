package com.demo.bank.conversion.controller;

import com.demo.bank.account.entity.Currency;
import com.demo.bank.conversion.service.CurrencyConversionService;
import com.demo.bank.utils.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionControllerTest {
    private MockMvc mockMvc;
    @Mock
    private CurrencyConversionService currencyConversionService;

    @BeforeEach
    void setup() {
        CurrencyConversionController controller = new CurrencyConversionController(currencyConversionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testConvert_success() throws Exception {
        when(currencyConversionService.convert(Currency.USD, Currency.EUR, 100.0)).thenReturn(90.0);
        mockMvc.perform(get("/api/currency/convert")
                .param("amount", "100.0")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.rate").value(0.9))
                .andExpect(jsonPath("$.convertedAmount").value(90.0));
        verify(currencyConversionService).convert(Currency.USD, Currency.EUR, 100.0);
    }

    @Test
    void testConvert_zeroAmount() throws Exception {
        when(currencyConversionService.convert(Currency.USD, Currency.EUR, 0.0)).thenReturn(0.0);
        mockMvc.perform(get("/api/currency/convert")
                .param("amount", "0.0")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(0.0))
                .andExpect(jsonPath("$.convertedAmount").value(0.0));
        verify(currencyConversionService).convert(Currency.USD, Currency.EUR, 0.0);
    }

    @Test
    void testConvert_negativeAmount() throws Exception {
        when(currencyConversionService.convert(Currency.USD, Currency.EUR, -50.0)).thenReturn(-45.0);
        mockMvc.perform(get("/api/currency/convert")
                .param("amount", "-50.0")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(-50.0))
                .andExpect(jsonPath("$.convertedAmount").value(-45.0));
        verify(currencyConversionService).convert(Currency.USD, Currency.EUR, -50.0);
    }

    @Test
    void testConvert_sameCurrency() throws Exception {
        when(currencyConversionService.convert(Currency.USD, Currency.USD, 100.0)).thenReturn(100.0);
        mockMvc.perform(get("/api/currency/convert")
                .param("amount", "100.0")
                .param("from", "USD")
                .param("to", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.convertedAmount").value(100.0))
                .andExpect(jsonPath("$.rate").value(1.0));
        verify(currencyConversionService).convert(Currency.USD, Currency.USD, 100.0);
    }

    @Test
    void testConvert_invalidCurrency() throws Exception {
        when(currencyConversionService.convert(Currency.XXX, Currency.YYY, 100.0)).thenThrow(new RuntimeException("Invalid currency code"));
        mockMvc.perform(get("/api/currency/convert")
                .param("amount", "100.0")
                .param("from", "XXX")
                .param("to", "YYY"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Invalid currency code"));
        verify(currencyConversionService).convert(Currency.XXX, Currency.YYY, 100.0);
    }
}
