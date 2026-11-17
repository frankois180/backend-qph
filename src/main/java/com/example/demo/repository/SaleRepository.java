package com.example.demo.repository;

import com.example.demo.entity.Sale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<Sale> findById(Long id);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Sale> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Sale> findByUser_UsernameOrderByCreatedAtDesc(String username);
}
