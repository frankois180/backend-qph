package com.example.demo.service;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.SaleResponse;

import java.util.List;

public interface SaleService {

    SaleResponse create(CreateSaleRequest request, String username);

    List<SaleResponse> findAll(String username, boolean admin);

    SaleResponse findById(Long id, String username, boolean admin);
}
