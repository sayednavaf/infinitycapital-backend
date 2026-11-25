package com.infinitycapital.api.market;

import com.infinitycapital.api.market.MarketModels.DashboardResponse;
import com.infinitycapital.api.market.MarketModels.MarketSnapshot;
import com.infinitycapital.api.market.MarketModels.StockAnalysis;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class MarketController {
    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/market/snapshot")
    public MarketSnapshot snapshot(@RequestParam(defaultValue = "1D") String period) {
        return marketDataService.snapshot(period);
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return new DashboardResponse(marketDataService.snapshot("1D"));
    }

    @GetMapping("/stocks/{symbol}/analysis")
    public StockAnalysis analysis(
            @PathVariable @Pattern(regexp = "[A-Za-z0-9._-]{1,20}", message = "Invalid stock symbol") String symbol) {
        return marketDataService.stockAnalysis(symbol);
    }
}
