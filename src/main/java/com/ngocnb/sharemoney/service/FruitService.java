package com.ngocnb.sharemoney.service;

import com.ngocnb.sharemoney.dto.request.FruitCreateRequest;
import com.ngocnb.sharemoney.dto.response.FruitResponse;
import org.springframework.data.domain.Page;

public interface FruitService {
    Page<FruitResponse> getAllFruits(int page, int size, String sortBy, String direction);

    Page<FruitResponse> searchFruitsByName(String name, int page, int size);

    FruitResponse getFruitById(Long id);

    FruitResponse createFruit(FruitCreateRequest request);

    FruitResponse updateFruit(Long id, FruitCreateRequest request);

    void deleteFruitById(Long id);

    long getTotalFruitCount();
}
