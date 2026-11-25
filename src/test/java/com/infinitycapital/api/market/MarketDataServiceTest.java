package com.infinitycapital.api.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class MarketDataServiceTest {
    private final MarketDataService service = new MarketDataService(
            RestClient.builder(), new ObjectMapper(), "", "", "Test provider");

    @Test
    void missingProviderReturnsUnavailableInsteadOfSampleQuotes() {
        MarketDataUnavailableException exception = assertThrows(
                MarketDataUnavailableException.class,
                () -> service.snapshot("1D"));

        assertEquals(
                "Market data unavailable. Configure an authorized provider using MARKET_DATA_API_URL.",
                exception.getMessage());
    }

    @Test
    void unsupportedChartRangeIsRejected() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.snapshot("2Y"));

        assertEquals("Unsupported period. Use 1D, 1W, 1M, 3M, or 1Y.", exception.getMessage());
    }
}
