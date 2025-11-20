package com.capitalot.repository;

import com.capitalot.model.Portfolio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    @EntityGraph(attributePaths = {"stocks", "stocks.stock"})
    List<Portfolio> findByUserId(Long userId);
    
    @EntityGraph(attributePaths = {"stocks", "stocks.stock"})
    @Query("SELECT p FROM Portfolio p WHERE p.id = :id")
    Optional<Portfolio> findByIdWithStocks(@Param("id") Long id);
}
