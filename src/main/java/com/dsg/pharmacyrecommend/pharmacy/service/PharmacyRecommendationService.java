package com.dsg.pharmacyrecommend.pharmacy.service;

import com.dsg.pharmacyrecommend.service.KakaoAddressSearchService;
import com.dsg.pharmacyrecommend.direction.dto.OutputDto;
import com.dsg.pharmacyrecommend.direction.entity.Direction;
import com.dsg.pharmacyrecommend.direction.service.Base62Service;
import com.dsg.pharmacyrecommend.direction.service.DirectionService;
import com.dsg.pharmacyrecommend.dto.DocumentDto;
import com.dsg.pharmacyrecommend.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private static final String ROAD_VIEW_BASE_URL = "https://map.kakao.com/link/roadview/";

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;
    private final Base62Service base62Service;

    @Value("${pharmacy.recommendation.base.url}")
    private String baseUrl;

    public List<OutputDto> recommendPharmacyList(String address) {

        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        if (Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentList())) {
            log.error("[PharmacyRecommendationService.recommendPharmacyList fail] Input address: {}", address);
            return Collections.emptyList();
        }

        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);
        log.info("[PharmacyRecommendationService.recommendPharmacyList] documentDto: {}", documentDto);

        // 공공기관 약국 데이터 및 거리계산 알고리즘 이용
        List<Direction> directionList = directionService.buildDirectionList(documentDto);
        // kakao 카테고리를 이용한 장소 검색 api 이용
//        List<Direction> directionList = directionService.buildDirectionListByCategoryApi(documentDto);

        log.info("[PharmacyRecommendationService.recommendPharmacyList] directionList: {}", directionList);

        return directionService.saveAll(directionList)
                .stream()
                .map(this::convertToOutputDto)
                .collect(Collectors.toList());
    }

    private OutputDto convertToOutputDto(Direction direction) {

        return OutputDto.builder()
                .pharmacyName(direction.getTargetPharmacyName())
                .pharmacyAddress(direction.getTargetAddress())
                .directionUrl(baseUrl + base62Service.encodeDirectionId(direction.getId()))
                .roadViewUrl(ROAD_VIEW_BASE_URL + direction.getTargetLatitude() + "," + direction.getTargetLongitude())
                .distance(String.format("%.2f km", direction.getDistance()))
                .build();
    }
}
