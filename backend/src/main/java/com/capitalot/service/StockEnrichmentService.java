package com.capitalot.service;

import com.capitalot.dto.AlphaVantageOverviewResponse;
import com.capitalot.dto.DailySnapshotDto;
import com.capitalot.dto.FinnhubMetricResponse;
import com.capitalot.dto.FinnhubProfileResponse;
import com.capitalot.dto.StockFundamentalsDto;
import com.capitalot.dto.StockPriceResponse;
import com.capitalot.dto.yahoofinance.YahooFinanceChartResponse;
import com.capitalot.dto.yahoofinance.YahooFinanceQuoteSummaryResponse;
import com.capitalot.model.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * Enrichissement complet pour la page détail :
     * - Logo Finnhub profile2 (toujours forcé)
     * - Snapshots historiques via chart Yahoo 1y/1d
     * - Alpha Vantage : Beta→risk, dividendes, analyst ratings
     */
    public Stock enrichStockFundamentals(Stock stock) {
        if (stock == null) return null;

        // 1. Logo depuis Finnhub profile2 (toujours forcé pour la page détail)
        try {
            finnhubService.getProfile(stock.getSymbol()).ifPresent(profile -> {
                if (profile.getLogo() != null && !profile.getLogo().isBlank()) {
                    stock.setLogoUrl(profile.getLogo());
                }
                if (profile.getMarketCapitalization() != null) stock.setMarketCapitalization(profile.getMarketCapitalization());
                if (profile.getShareOutstanding() != null) stock.setShareOutstanding(profile.getShareOutstanding());
                if (profile.getWeburl() != null && !profile.getWeburl().isBlank()) stock.setWeburl(profile.getWeburl());
                if (profile.getExchange() != null && !profile.getExchange().isBlank()) stock.setExchange(profile.getExchange());
            });
        } catch (Exception e) {
            log.warn("Could not fetch Finnhub profile for logo {}: {}", stock.getSymbol(), e.getMessage());
        }

        // 2. Snapshots historiques depuis Yahoo Finance chart 1y/1d
        try {
            yahooFinanceService.getChartData(stock.getSymbol(), "1y", "1d").ifPresent(chart -> {
                stock.setHistoricalSnapshots(buildHistoricalSnapshots(chart));
            });
        } catch (Exception e) {
            log.warn("Could not build historical snapshots for {}: {}", stock.getSymbol(), e.getMessage());
        }

        // 3. Alpha Vantage : Beta→risk, dividendes, analyst ratings
        try {
            alphaVantageService.getOverview(stock.getSymbol()).ifPresent(overview -> {
                applyAlphaVantageOverview(stock, overview);
            });
        } catch (Exception e) {
            log.warn("Could not enrich fundamentals for {}: {}", stock.getSymbol(), e.getMessage());
        }

        // 4. Finnhub basic financials
        try {
            finnhubService.getMetrics(stock.getSymbol()).ifPresent(metrics -> {
                stock.setFundamentals(buildFundamentals(metrics));
            });
        } catch (Exception e) {
            log.warn("Could not fetch Finnhub metrics for {}: {}", stock.getSymbol(), e.getMessage());
        }

        return stock;
    }

    private StockFundamentalsDto buildFundamentals(FinnhubMetricResponse response) {
        Map<String, Object> m = response.getMetric();
        if (m == null) return null;

        return StockFundamentalsDto.builder()
                .peTTM(getMetricDouble(m, "peExclExtraTTM"))
                .forwardPE(getMetricDouble(m, "forwardPE"))
                .pb(getMetricDouble(m, "pb"))
                .psTTM(getMetricDouble(m, "psTTM"))
                .evEbitdaTTM(getMetricDouble(m, "evEbitdaTTM"))
                .epsAnnual(getMetricDouble(m, "epsAnnual"))
                .epsTTM(getMetricDouble(m, "epsTTM"))
                .epsGrowthTTMYoy(getMetricDouble(m, "epsGrowthTTMYoy"))
                .epsGrowth5Y(getMetricDouble(m, "epsGrowth5Y"))
                .grossMarginTTM(getMetricDouble(m, "grossMarginTTM"))
                .operatingMarginTTM(getMetricDouble(m, "operatingMarginTTM"))
                .netProfitMarginTTM(getMetricDouble(m, "netProfitMarginTTM"))
                .roeTTM(getMetricDouble(m, "roeTTM"))
                .roaTTM(getMetricDouble(m, "roaTTM"))
                .revenueGrowthTTMYoy(getMetricDouble(m, "revenueGrowthTTMYoy"))
                .revenueGrowth5Y(getMetricDouble(m, "revenueGrowth5Y"))
                .return5D(getMetricDouble(m, "5DayPriceReturnDaily"))
                .return13W(getMetricDouble(m, "13WeekPriceReturnDaily"))
                .return26W(getMetricDouble(m, "26WeekPriceReturnDaily"))
                .return52W(getMetricDouble(m, "52WeekPriceReturnDaily"))
                .returnYTD(getMetricDouble(m, "yearToDatePriceReturnDaily"))
                .dividendPerShareAnnual(getMetricDouble(m, "dividendPerShareAnnual"))
                .dividendYieldAnnual(getMetricDouble(m, "dividendYieldIndicatedAnnual"))
                .dividendGrowthRate5Y(getMetricDouble(m, "dividendGrowthRate5Y"))
                .beta(getMetricDouble(m, "beta"))
                .build();
    }

    private Double getMetricDouble(Map<String, Object> m, String key) {
        Object val = m.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return null;
    }

    private List<DailySnapshotDto> buildHistoricalSnapshots(YahooFinanceChartResponse chart) {
        List<DailySnapshotDto> snapshots = new ArrayList<>();
        if (chart.getChart() == null || chart.getChart().getResult() == null || chart.getChart().getResult().isEmpty()) {
            return snapshots;
        }

        YahooFinanceChartResponse.Result result = chart.getChart().getResult().get(0);
        List<Long> timestamps = result.getTimestamp();
        if (timestamps == null || timestamps.isEmpty()) return snapshots;

        if (result.getIndicators() == null || result.getIndicators().getQuote() == null
                || result.getIndicators().getQuote().isEmpty()) return snapshots;

        YahooFinanceChartResponse.Quote q = result.getIndicators().getQuote().get(0);
        List<Double> opens = q.getOpen();
        List<Double> highs = q.getHigh();
        List<Double> lows = q.getLow();
        List<Double> closes = q.getClose();
        List<Long> volumes = q.getVolume();

        int size = timestamps.size();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Indices des périodes à afficher (on remonte depuis la fin)
        int[][] periodsConfig = {
            { size - 2, 0 },   // Hier (index, offset pour changePercent)
            { size - 6, size - 7 },   // Il y a 1 semaine
            { size - 22, size - 23 }, // Il y a 1 mois
            { 0, -1 }                 // Il y a 1 an (pas de précédent)
        };
        String[] labels = { "Hier", "Il y a 1 semaine", "Il y a 1 mois", "Il y a 1 an" };

        for (int i = 0; i < periodsConfig.length; i++) {
            int idx = Math.max(0, periodsConfig[i][0]);
            int prevIdx = periodsConfig[i][1];

            if (idx >= size) continue;

            String date = Instant.ofEpochSecond(timestamps.get(idx))
                    .atZone(ZoneId.systemDefault()).toLocalDate().format(fmt);

            Double close = safeGet(closes, idx);
            Double prevClose = (prevIdx >= 0 && prevIdx < size) ? safeGet(closes, prevIdx) : null;

            Double changePercent = null;
            if (close != null && prevClose != null && prevClose != 0.0) {
                changePercent = (close - prevClose) / prevClose * 100;
            }

            snapshots.add(DailySnapshotDto.builder()
                    .period(labels[i])
                    .date(date)
                    .open(safeGet(opens, idx))
                    .high(safeGet(highs, idx))
                    .low(safeGet(lows, idx))
                    .close(close)
                    .volume(volumes != null && idx < volumes.size() ? volumes.get(idx) : null)
                    .changePercent(changePercent)
                    .build());
        }

        return snapshots;
    }

    private Double safeGet(List<Double> list, int idx) {
        if (list == null || idx < 0 || idx >= list.size()) return null;
        Double v = list.get(idx);
        return (v != null && v != 0.0) ? v : null;
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
