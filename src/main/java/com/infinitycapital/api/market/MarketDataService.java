package com.infinitycapital.api.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitycapital.api.market.MarketModels.ChartPoint;
import com.infinitycapital.api.market.MarketModels.MarketSnapshot;
import com.infinitycapital.api.market.MarketModels.Quote;
import com.infinitycapital.api.market.MarketModels.StockAnalysis;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class MarketDataService {
    private static final List<String> SUPPORTED_PERIODS = List.of("1D", "1W", "1M", "3M", "1Y");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String providerUrl;
    private final String providerToken;
    private final String sourceName;

    public MarketDataService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${market.data.api-url:}") String providerUrl,
            @Value("${market.data.api-token:}") String providerToken,
            @Value("${market.data.source:Authorized market data provider}") String sourceName) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.providerUrl = providerUrl.replaceAll("/+$", "");
        this.providerToken = providerToken;
        this.sourceName = sourceName;
    }

    public MarketSnapshot snapshot(String period) {
        if (!SUPPORTED_PERIODS.contains(period)) {
            throw new IllegalArgumentException("Unsupported period. Use 1D, 1W, 1M, 3M, or 1Y.");
        }
        JsonNode payload = fetch(providerUrl + "/snapshot?period=" + period);
        JsonNode quotesNode = payload.path("quotes");
        JsonNode seriesNode = payload.path("series");
        if (!quotesNode.isArray() || !seriesNode.isArray()) {
            throw new MarketDataUnavailableException("The market-data provider returned an unsupported response.");
        }
        return new MarketSnapshot(
                "AVAILABLE",
                text(payload, "source", sourceName),
                instant(payload, "updatedAt"),
                text(payload, "marketStatus", "UNKNOWN"),
                longValue(payload, "volume"),
                quotes(quotesNode),
                quotes(payload.path("instruments")),
                quotes(payload.path("gainers")),
                quotes(payload.path("losers")),
                series(seriesNode));
    }

    public StockAnalysis stockAnalysis(String symbol) {
        String normalizedSymbol = symbol.toUpperCase();
        JsonNode payload = fetch(providerUrl + "/stocks/" + normalizedSymbol + "/analysis");
        if (!payload.hasNonNull("summary")) {
            throw new MarketDataUnavailableException("The research provider returned no analysis for " + normalizedSymbol + ".");
        }
        return new StockAnalysis(
                text(payload, "symbol", normalizedSymbol),
                payload.path("summary").asText(),
                text(payload, "source", sourceName),
                instant(payload, "updatedAt"));
    }

    private JsonNode fetch(String url) {
        if (providerUrl.isBlank()) {
            throw new MarketDataUnavailableException("Market data unavailable. Configure an authorized provider using MARKET_DATA_API_URL.");
        }
        try {
            JsonNode payload = restClient.get()
                    .uri(URI.create(url))
                    .headers(headers -> {
                        if (!providerToken.isBlank()) {
                            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + providerToken);
                        }
                    })
                    .retrieve()
                    .body(JsonNode.class);
            if (payload == null || payload.isNull()) {
                throw new MarketDataUnavailableException("The market-data provider returned an empty response.");
            }
            return payload;
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new MarketDataUnavailableException("Market data provider is unavailable. Please try again later.", exception);
        }
    }

    private List<Quote> quotes(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        List<Quote> result = new ArrayList<>();
        for (JsonNode quote : node) {
            result.add(objectMapper.convertValue(quote, Quote.class));
        }
        return result;
    }

    private List<ChartPoint> series(JsonNode node) {
        List<ChartPoint> result = new ArrayList<>();
        for (JsonNode point : node) {
            result.add(objectMapper.convertValue(point, ChartPoint.class));
        }
        return result;
    }

    private String text(JsonNode node, String field, String fallback) {
        return node.hasNonNull(field) ? node.path(field).asText() : fallback;
    }

    private java.time.Instant instant(JsonNode node, String field) {
        if (!node.hasNonNull(field)) {
            return null;
        }
        try {
            return java.time.Instant.parse(node.path(field).asText());
        } catch (java.time.format.DateTimeParseException exception) {
            throw new MarketDataUnavailableException("The market-data provider returned an invalid " + field + " timestamp.", exception);
        }
    }

    private Long longValue(JsonNode node, String field) {
        return node.hasNonNull(field) && node.path(field).canConvertToLong() ? node.path(field).asLong() : null;
    }
}
