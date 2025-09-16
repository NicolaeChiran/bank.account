package com.demo.bank.conversion.service;

import com.demo.bank.conversion.repository.CurrencyConversionRepository;
import com.demo.bank.account.entity.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyConversionServiceTest {
    @Mock
    private CurrencyConversionRepository currencyConversionRepository;
    @InjectMocks
    private CurrencyConversionService currencyConversionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConvert_success() {
        when(currencyConversionRepository.getConversionRate(Currency.USD, Currency.EUR)).thenReturn(0.9);
        double result = currencyConversionService.convert(Currency.USD, Currency.EUR, 100.0);
        assertEquals(90.0, result);
        verify(currencyConversionRepository).getConversionRate(Currency.USD, Currency.EUR);
    }

    @Test
    void testConvert_sameCurrency() {
        when(currencyConversionRepository.getConversionRate(Currency.USD, Currency.USD)).thenReturn(1.0);
        double result = currencyConversionService.convert(Currency.USD, Currency.USD, 50.0);
        assertEquals(50.0, result);
        verify(currencyConversionRepository).getConversionRate(Currency.USD, Currency.USD);
    }

    @Test
    void testConvert_zeroAmount() {
        when(currencyConversionRepository.getConversionRate(Currency.USD, Currency.EUR)).thenReturn(0.9);
        double result = currencyConversionService.convert(Currency.USD, Currency.EUR, 0.0);
        assertEquals(0.0, result);
        verify(currencyConversionRepository).getConversionRate(Currency.USD, Currency.EUR);
    }

    @Test
    void testConvert_negativeAmount() {
        when(currencyConversionRepository.getConversionRate(Currency.USD, Currency.EUR)).thenReturn(0.9);
        double result = currencyConversionService.convert(Currency.USD, Currency.EUR, -100.0);
        assertEquals(-90.0, result);
        verify(currencyConversionRepository).getConversionRate(Currency.USD, Currency.EUR);
    }

    @Test
    void testConvert_invalidCurrency_throws() {
        when(currencyConversionRepository.getConversionRate(Currency.XXX, Currency.YYY)).thenThrow(new RuntimeException("Invalid currency code"));
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            currencyConversionService.convert(Currency.XXX, Currency.YYY, 100.0)
        );
        assertEquals("Invalid currency code", ex.getMessage());
        verify(currencyConversionRepository).getConversionRate(Currency.XXX, Currency.YYY);
    }
}
