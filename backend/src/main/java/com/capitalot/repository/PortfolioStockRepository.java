package com.capitalot.repository;

import com.capitalot.model.PortfolioStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PortfolioStockRepository extends JpaRepository<PortfolioStock, Long> {
    List<PortfolioStock> findByPortfolioId(Long portfolioId);
    
    @Query("SELECT ps FROM PortfolioStock ps WHERE ps.portfolio.user.id = :userId " +
           "AND ps.purchaseDate BETWEEN :startDate AND :endDate")
    List<PortfolioStock> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
