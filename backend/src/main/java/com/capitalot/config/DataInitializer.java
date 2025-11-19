package com.capitalot.config;

import com.capitalot.service.StockSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final StockSearchService stockSearchService;
    
    @Override
    public void run(String... args) {
        stockSearchService.initializePopularStocks();
    }
}
