package com.capitalot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockFundamentalsDto {
    // Valorisation
    private Double peTTM;
    private Double forwardPE;
    private Double pb;
    private Double psTTM;
    private Double evEbitdaTTM;

    // BPA
    private Double epsAnnual;
    private Double epsTTM;
    private Double epsGrowthTTMYoy;
    private Double epsGrowth5Y;

    // Marges
    private Double grossMarginTTM;
    private Double operatingMarginTTM;
    private Double netProfitMarginTTM;

    // Rendements
    private Double roeTTM;
    private Double roaTTM;

    // Croissance revenus
    private Double revenueGrowthTTMYoy;
    private Double revenueGrowth5Y;

    // Performance prix
    private Double return5D;
    private Double return13W;
    private Double return26W;
    private Double return52W;
    private Double returnYTD;

    // Dividende
    private Double dividendPerShareAnnual;
    private Double dividendYieldAnnual;
    private Double dividendGrowthRate5Y;

    // Risque
    private Double beta;
}
