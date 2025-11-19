package com.capitalot.repository;

import com.capitalot.model.PortfolioHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PortfolioHistoryRepository extends JpaRepository<PortfolioHistory, Long> {
    
    @Query("SELECT h FROM PortfolioHistory h WHERE h.portfolio.id = :portfolioId AND h.timestamp >= :startDate ORDER BY h.timestamp ASC")
    List<PortfolioHistory> findByPortfolioIdAndTimestampAfter(@Param("portfolioId") Long portfolioId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT h FROM PortfolioHistory h WHERE h.portfolio.user.email = :email AND h.timestamp >= :startDate ORDER BY h.timestamp ASC")
    List<PortfolioHistory> findByUserEmailAndTimestampAfter(@Param("email") String email, @Param("startDate") LocalDateTime startDate);
}
