package com.capitalot.service;

import com.capitalot.dto.AlphaVantageOverviewResponse;
import com.capitalot.dto.FinnhubProfileResponse;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.dto.yahoofinance.YahooFinanceChartResponse;
import com.capitalot.dto.yahoofinance.YahooFinanceQuoteSummaryResponse;
import com.capitalot.model.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockEnrichmentService {

    private final StockPriceService stockPriceService;
    private final YahooFinanceService yahooFinanceService;
    private final FinnhubService finnhubService;
    private final AlphaVantageService alphaVantageService;

    public Stock enrichStockWithRealPrice(Stock stock) {
        if (stock == null) return null;

        try {
            StockPriceResponse priceData = stockPriceService.getStockPrice(stock.getSymbol());

            if (priceData != null && priceData.getCurrentPrice() != null) {
                stock.setCurrentPrice(priceData.getCurrentPrice().doubleValue());
                if (priceData.getOpenPrice() != null) stock.setOpenPrice(priceData.getOpenPrice().doubleValue());
                if (priceData.getHighPrice() != null) stock.setHighPrice(priceData.getHighPrice().doubleValue());
                if (priceData.getLowPrice() != null) stock.setLowPrice(priceData.getLowPrice().doubleValue());
                if (priceData.getPreviousClose() != null) stock.setPreviousClose(priceData.getPreviousClose().doubleValue());
                if (priceData.getFiftyTwoWeekHigh() != null) stock.setFiftyTwoWeekHigh(priceData.getFiftyTwoWeekHigh().doubleValue());
                if (priceData.getFiftyTwoWeekLow() != null) stock.setFiftyTwoWeekLow(priceData.getFiftyTwoWeekLow().doubleValue());

                stock.setVolume(priceData.getVolume());

                if (priceData.getPreviousClose() != null) {
                    stock.setDailyChange(priceData.getCurrentPrice().subtract(priceData.getPreviousClose()).doubleValue());
                }

                if (priceData.getChangePercent() != null) {
                    stock.setDailyChangePercentage(priceData.getChangePercent().doubleValue());
                }

                stock.setLastPriceUpdate(priceData.getLastUpdated());
            }

            // Enrichissement des détails si logo manquant ou secteur inconnu
            if (stock.getLogoUrl() == null || stock.getLogoUrl().isBlank()
                    || stock.getSector() == null || "Unknown".equals(stock.getSector())) {
                enrichStockDetails(stock);
            }
        } catch (Exception e) {
            log.error("Error enriching stock {}: {}", stock.getSymbol(), e.getMessage());
        }

        return stock;
    }

    /**
     * Enrichissement complet avec fondamentaux (Beta→risk, dividendes, analyst ratings).
     * Appelé uniquement pour la page détail d'une action.
     */
    public Stock enrichStockFundamentals(Stock stock) {
        if (stock == null) return null;

        // Max du jour précédent (5d/1d chart → avant-dernier point)
        try {
            yahooFinanceService.getChartData(stock.getSymbol(), "5d", "1d").ifPresent(chart -> {
                if (chart.getChart() == null || chart.getChart().getResult() == null || chart.getChart().getResult().isEmpty()) return;
                YahooFinanceChartResponse.Result result = chart.getChart().getResult().get(0);
                if (result.getIndicators() == null || result.getIndicators().getQuote() == null
                        || result.getIndicators().getQuote().isEmpty()) return;

                List<Double> highs = result.getIndicators().getQuote().get(0).getHigh();
                if (highs != null && highs.size() >= 2) {
                    int prevIdx = highs.size() - 2;
                    while (prevIdx >= 0 && (highs.get(prevIdx) == null || highs.get(prevIdx) == 0.0)) prevIdx--;
                    if (prevIdx >= 0) stock.setPreviousDayHigh(highs.get(prevIdx));
                }
            });
        } catch (Exception e) {
            log.warn("Could not fetch previous day high for {}: {}", stock.getSymbol(), e.getMessage());
        }

        // Alpha Vantage : Beta→risk, dividendes, analyst ratings
        try {
            alphaVantageService.getOverview(stock.getSymbol()).ifPresent(overview -> {
                applyAlphaVantageOverview(stock, overview);
            });
        } catch (Exception e) {
            log.warn("Could not enrich fundamentals for {}: {}", stock.getSymbol(), e.getMessage());
        }

        return stock;
    }

    private void enrichStockDetails(Stock stock) {
        // 1. Finnhub profile2 : logo, exchange, industry, name
        try {
            finnhubService.getProfile(stock.getSymbol()).ifPresent(profile -> {
                if (profile.getLogo() != null && !profile.getLogo().isBlank()) {
                    stock.setLogoUrl(profile.getLogo());
                }
                if (profile.getExchange() != null && !profile.getExchange().isBlank()) {
                    stock.setExchange(profile.getExchange());
                }
                if (profile.getFinnhubIndustry() != null && !profile.getFinnhubIndustry().isBlank()
                        && (stock.getIndustry() == null || "Unknown".equals(stock.getIndustry()))) {
                    stock.setIndustry(profile.getFinnhubIndustry());
                }
                if (profile.getName() != null && !profile.getName().isBlank()
                        && (stock.getName() == null || stock.getName().equals(stock.getSymbol()))) {
                    stock.setName(profile.getName());
                }
                if (profile.getCurrency() != null && !profile.getCurrency().isBlank()) {
                    stock.setCurrency(profile.getCurrency());
                }
                if (profile.getMarketCapitalization() != null) {
                    stock.setMarketCapitalization(profile.getMarketCapitalization());
                }
                if (profile.getShareOutstanding() != null) {
                    stock.setShareOutstanding(profile.getShareOutstanding());
                }
                if (profile.getWeburl() != null && !profile.getWeburl().isBlank()) {
                    stock.setWeburl(profile.getWeburl());
                }
            });
        } catch (Exception e) {
            log.warn("Could not enrich from Finnhub profile for {}: {}", stock.getSymbol(), e.getMessage());
        }

        // 2. Yahoo Finance quote summary : secteur, industrie, description, logo en fallback
        if (stock.getSector() == null || "Unknown".equals(stock.getSector())) {
            try {
                yahooFinanceService.getQuoteSummary(stock.getSymbol()).ifPresent(summary -> {
                    if (summary.getQuoteSummary() != null && summary.getQuoteSummary().getResult() != null
                            && !summary.getQuoteSummary().getResult().isEmpty()) {
                        YahooFinanceQuoteSummaryResponse.Result result = summary.getQuoteSummary().getResult().get(0);

                        if (result.getAssetProfile() != null) {
                            YahooFinanceQuoteSummaryResponse.AssetProfile profile = result.getAssetProfile();
                            if (profile.getSector() != null) stock.setSector(profile.getSector());
                            if (profile.getIndustry() != null && (stock.getIndustry() == null || "Unknown".equals(stock.getIndustry()))) {
                                stock.setIndustry(profile.getIndustry());
                            }
                            if (profile.getLongBusinessSummary() != null && (stock.getDescription() == null || stock.getDescription().isBlank())) {
                                stock.setDescription(profile.getLongBusinessSummary());
                            }
                            if (profile.getWebsite() != null && (stock.getLogoUrl() == null || stock.getLogoUrl().isBlank())) {
                                String domain = profile.getWebsite()
                                        .replace("http://", "")
                                        .replace("https://", "")
                                        .replace("www.", "")
                                        .split("/")[0];
                                stock.setLogoUrl("https://logo.clearbit.com/" + domain);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                log.warn("Could not enrich details from Yahoo for {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
    }

    private void applyAlphaVantageOverview(Stock stock, AlphaVantageOverviewResponse overview) {
        // Risk via Beta (Beta * 5, borné 1-10)
        if (overview.getBeta() != null && !overview.getBeta().equals("None")) {
            try {
                double beta = Double.parseDouble(overview.getBeta().trim());
                double risk = Math.min(10.0, Math.max(1.0, beta * 5.0));
                stock.setRisk(Math.round(risk * 10.0) / 10.0);
            } catch (NumberFormatException ignored) {}
        }

        // Dividende annuel
        if (overview.getDividendPerShare() != null && !overview.getDividendPerShare().equals("None")) {
            try {
                stock.setAnnualDividend(Double.parseDouble(overview.getDividendPerShare().trim()));
            } catch (NumberFormatException ignored) {}
        }

        // Analyst ratings → longPercentage, shortPercentage, marketScore
        int strongBuy = parseIntSafe(overview.getAnalystRatingStrongBuy());
        int buy = parseIntSafe(overview.getAnalystRatingBuy());
        int hold = parseIntSafe(overview.getAnalystRatingHold());
        int sell = parseIntSafe(overview.getAnalystRatingSell());
        int strongSell = parseIntSafe(overview.getAnalystRatingStrongSell());
        int total = strongBuy + buy + hold + sell + strongSell;

        if (total > 0) {
            stock.setLongPercentage((strongBuy + buy) * 100.0 / total);
            stock.setShortPercentage((sell + strongSell) * 100.0 / total);
            double score = (strongBuy * 10.0 + buy * 7.5 + hold * 5.0 + sell * 2.5) / total;
            stock.setMarketScore(Math.round(score * 10.0) / 10.0);
        }

        // Secteur / industrie en fallback
        if ((stock.getSector() == null || "Unknown".equals(stock.getSector()))
                && overview.getSector() != null && !overview.getSector().isBlank()) {
            stock.setSector(overview.getSector());
        }
        if ((stock.getIndustry() == null || "Unknown".equals(stock.getIndustry()))
                && overview.getIndustry() != null && !overview.getIndustry().isBlank()) {
            stock.setIndustry(overview.getIndustry());
        }

        // Description en fallback
        if ((stock.getDescription() == null || stock.getDescription().isBlank())
                && overview.getDescription() != null && !overview.getDescription().isBlank()) {
            stock.setDescription(overview.getDescription());
        }
    }

    public List<Stock> enrichStocksWithRealPrices(List<Stock> stocks) {
        if (stocks == null) return List.of();
        return stocks.stream()
                .map(this::enrichStockWithRealPrice)
                .toList();
    }

    private int parseIntSafe(String value) {
        if (value == null || value.equals("None") || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
