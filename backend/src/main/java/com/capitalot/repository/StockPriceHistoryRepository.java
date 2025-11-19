package com.capitalot.repository;

import com.capitalot.model.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {
    
    @Query("SELECT h FROM StockPriceHistory h WHERE h.stock.id = :stockId AND h.timestamp >= :startDate ORDER BY h.timestamp ASC")
    List<StockPriceHistory> findByStockIdAndTimestampAfter(@Param("stockId") Long stockId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT h FROM StockPriceHistory h WHERE h.stock.id = :stockId ORDER BY h.timestamp DESC")
    List<StockPriceHistory> findByStockIdOrderByTimestampDesc(@Param("stockId") Long stockId);
}
