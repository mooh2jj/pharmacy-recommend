package com.dsg.pharmacyrecommend.direction.controller;

import com.dsg.pharmacyrecommend.direction.dto.InputDto;
import com.dsg.pharmacyrecommend.direction.dto.OutputDto;
import com.dsg.pharmacyrecommend.pharmacy.service.PharmacyRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direction")
public class DirectionController {

    private final PharmacyRecommendationService pharmacyRecommendationService;


    @PostMapping("/search")
    public ResponseEntity<List<OutputDto>> searchPharmacy(@RequestBody InputDto inputDto) {
        log.info("[DirectionController.searchPharmacy] inputDto: {}", inputDto);
        List<OutputDto> outputDtos = pharmacyRecommendationService.recommendPharmacyList(inputDto.getAddress());

        return ResponseEntity.ok(outputDtos);
    }
}
