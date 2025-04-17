package com.dsg.pharmacyrecommend.domain.direction.controller;

import com.dsg.pharmacyrecommend.domain.direction.dto.InputDto;
import com.dsg.pharmacyrecommend.domain.direction.dto.OutputDto;
import com.dsg.pharmacyrecommend.domain.direction.service.DirectionService;
import com.dsg.pharmacyrecommend.domain.pharmacy.service.PharmacyRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direction")
public class DirectionController {

    private final PharmacyRecommendationService pharmacyRecommendationService;
    private final DirectionService directionService;


    @PostMapping("/search")
    public ResponseEntity<List<OutputDto>> searchPharmacy(@RequestBody InputDto inputDto) {
        log.info("[DirectionController.searchPharmacy] inputDto: {}", inputDto);
        List<OutputDto> outputDtos = pharmacyRecommendationService.recommendPharmacyList(inputDto.getAddress());

        return ResponseEntity.ok(outputDtos);
    }

    @GetMapping("/{encodedId}")
    public ResponseEntity<?> getPharmacy(@PathVariable String encodedId) {
        log.info("[DirectionController.getPharmacy] encodedId: {}", encodedId);
        String directionUrlById = directionService.findDirectionUrlById(encodedId);
        log.info("[DirectionController.getPharmacy] directionUrlById: {}", directionUrlById);
        return ResponseEntity.ok(directionUrlById);
    }
}
