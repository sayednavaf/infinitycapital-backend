package com.infinitycapital.api.market;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class MarketModels {
    private MarketModels() {
    }

    public record Quote(
            String symbol,
            String name,
            String type,
            String exchange,
            BigDecimal price,
            BigDecimal change,
            BigDecimal changePercent,
            Long volume,
            Instant timestamp) {
    }

    public record ChartPoint(String time, BigDecimal value) {
    }

    public record MarketSnapshot(
            String status,
            String source,
            Instant updatedAt,
            String marketStatus,
            Long volume,
            List<Quote> quotes,
            List<Quote> instruments,
            List<Quote> gainers,
            List<Quote> losers,
            List<ChartPoint> series) {
    }

    public record StockAnalysis(String symbol, String summary, String source, Instant updatedAt) {
    }

    public record DashboardResponse(MarketSnapshot market) {
    }
}
