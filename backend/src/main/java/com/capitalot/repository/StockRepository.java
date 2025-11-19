package com.capitalot.repository;

import com.capitalot.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbol(String symbol);
    
    @Query("SELECT s FROM Stock s WHERE s.isPopular = true ORDER BY s.name")
    List<Stock> findPopularStocks();
    
    @Query("SELECT s FROM Stock s WHERE " +
           "LOWER(s.symbol) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY s.isPopular DESC, s.name")
    List<Stock> searchStocks(@Param("query") String query);
}
