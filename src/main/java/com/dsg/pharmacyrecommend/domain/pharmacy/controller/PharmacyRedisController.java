package com.dsg.pharmacyrecommend.domain.pharmacy.controller;

import com.dsg.pharmacyrecommend.domain.pharmacy.cache.PharmacyRedisTemplateService;
import com.dsg.pharmacyrecommend.domain.pharmacy.dto.PharmacyDto;
import com.dsg.pharmacyrecommend.domain.pharmacy.service.PharmacyRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pharmacy")
public class PharmacyRedisController {


    private final PharmacyRepositoryService pharmacyRepositoryService;
    private final PharmacyRedisTemplateService pharmacyRedisTemplateService;

    /**
     * 처음 약국 정보를 Redis에 저장
     * @return success
     */
    @GetMapping("/redis/save")
    public String savePharmacyRedis() {
        log.info("[PharmacyRedisController.savePharmacyRedis] start");
        List<PharmacyDto> pharmacyDtoList = pharmacyRepositoryService.findAll()
                .stream().map(pharmacy -> PharmacyDto.builder()
                        .id(pharmacy.getId())
                        .pharmacyName(pharmacy.getPharmacyName())
                        .pharmacyAddress(pharmacy.getPharmacyAddress())
                        .latitude(pharmacy.getLatitude())
                        .longitude(pharmacy.getLongitude())
                        .build())
                .toList();

        pharmacyDtoList.forEach(pharmacyRedisTemplateService::save);
        return "success";
    }
}
