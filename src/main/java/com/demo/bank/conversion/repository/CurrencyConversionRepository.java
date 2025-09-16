package com.demo.bank.conversion.repository;

import com.demo.bank.account.entity.Currency;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.json.JSONObject;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class CurrencyConversionRepository {
    private static final String FRANKFURTER_API_URL = "https://api.frankfurter.dev/v1/latest";
    //documentation https://frankfurter.dev/
    private final RestTemplate restTemplate;

    public CurrencyConversionRepository() {
        this.restTemplate = createRestTemplateWithTrustedSSL();
    }

    private RestTemplate createRestTemplateWithTrustedSSL() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder.create()
                            .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, (hostname, session) -> true))
                            .build())
                    .build();

            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        } catch (Exception e) {
            log.warn("Failed to create SSL-enabled RestTemplate, using default: {}", e.getMessage());
            return new RestTemplate();
        }
    }

    public double getConversionRate(Currency from, Currency to) {
        try {
            // Try to fetch real conversion rate from Frankfurter API
            String url = UriComponentsBuilder.fromUriString(FRANKFURTER_API_URL)
                    .queryParam("base", from.toString())
                    .queryParam("symbols", to.toString())
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JSONObject json = new JSONObject(response.getBody());
            JSONObject rates = json.getJSONObject("rates");
            return rates.getDouble(to.toString());
        } catch (Exception e) {
            // Fallback to mock rates if API fails
            log.warn("Frankfurter API failed, using mock rates: {}", e.getMessage());
            return getMockConversionRate(from.toString(), to.toString());
        }
    }

    private double getMockConversionRate(String from, String to) {
        if (from.equals(to)) return 1.0;

        if (from.equals("USD") && to.equals("EUR")) return 0.85;
        if (from.equals("USD") && to.equals("RON")) return 4.5;
        if (from.equals("EUR") && to.equals("USD")) return 1.18;
        if (from.equals("EUR") && to.equals("RON")) return 5.0;
        if (from.equals("RON") && to.equals("USD")) return 0.22;
        if (from.equals("RON") && to.equals("EUR")) return 0.20;

        // If not found, throw error
        throw new RuntimeException("Currency conversion not supported: from " + from + " to " + to);
    }
}
