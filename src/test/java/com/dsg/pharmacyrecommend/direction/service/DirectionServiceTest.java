package com.dsg.pharmacyrecommend.direction.service;

import com.dsg.pharmacyrecommend.direction.entity.Direction;
import com.dsg.pharmacyrecommend.direction.repository.DirectionRepository;
import com.dsg.pharmacyrecommend.dto.DocumentDto;
import com.dsg.pharmacyrecommend.pharmacy.dto.PharmacyDto;
import com.dsg.pharmacyrecommend.pharmacy.service.PharmacySearchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DirectionServiceTest {

    @Mock
    private PharmacySearchService pharmacySearchService;
    
    @Mock
    private DirectionRepository directionRepository;
    
    @Mock
    private Base62Service base62Service;

    private DirectionService directionService;
    
    private static final String DIRECTION_BASE_URL = "https://map.kakao.com/link/map/";

    @BeforeEach
    void setUp() {
        directionService = new DirectionService(pharmacySearchService, directionRepository, base62Service, null);
    }

    @Test
    @DisplayName("입력값이 null일 때 빈 리스트를 반환한다")
    void buildDirectionListWithNullInput() {
        // given
        DocumentDto documentDto = null;

        // when
        List<Direction> result = directionService.buildDirectionList(documentDto);

        // then
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("반경 10km 이내의 약국만 검색되어야 한다")
    void buildDirectionListWithinRadius() {
        // given
        DocumentDto documentDto = DocumentDto.builder()
                .addressName("서울특별시 성북구 종암동")
                .latitude(37.596907)        // 종암동 기준점
                .longitude(127.037803)
                .build();

        List<PharmacyDto> pharmacyDtoList = Arrays.asList(
                PharmacyDto.builder()
                        .pharmacyName("약국1")    // 반경 1km 이내
                        .pharmacyAddress("서울특별시 성북구 종암로")
                        .latitude(37.602030)
                        .longitude(127.037033)
                        .build(),
                PharmacyDto.builder()
                        .pharmacyName("약국2")    // 반경 5km 이내
                        .pharmacyAddress("서울특별시 성북구 화랑로")
                        .latitude(37.606203)
                        .longitude(127.042567)
                        .build(),
                PharmacyDto.builder()
                        .pharmacyName("약국3")    // 반경 15km 밖
                        .pharmacyAddress("서울특별시 강남구 역삼동")
                        .latitude(37.500713)
                        .longitude(127.036547)
                        .build(),
                PharmacyDto.builder()
                        .pharmacyName("약국4")    // 반경 30km 밖
                        .pharmacyAddress("서울특별시 강서구 마곡동")
                        .latitude(37.557667)
                        .longitude(126.835960)
                        .build()
        );

        when(pharmacySearchService.searchPharmacyDtoList()).thenReturn(pharmacyDtoList);

        // when
        List<Direction> results = directionService.buildDirectionList(documentDto);

        log.info("results: {}", results);
        // then
        assertThat(results).hasSize(2); // 10km 이내의 약국만 포함되어야 함
        assertThat(results.get(0).getTargetPharmacyName()).isEqualTo("약국1");
        assertThat(results.get(1).getTargetPharmacyName()).isEqualTo("약국2");
        
        // 거리 검증
        assertThat(results.get(0).getDistance()).isLessThan(10.0);
        assertThat(results.get(1).getDistance()).isLessThan(10.0);
    }
    
    @Test
    @DisplayName("findDirectionUrlById: 약국 방향 URL을 정상적으로 반환한다")
    void findDirectionUrlById() {
        // given
        String encodedId = "r";
        Long decodedId = 1L;
        String pharmacyName = "약국명";
        double latitude = 37.5960;
        double longitude = 127.0371;
        
        Direction direction = Direction.builder()
                .targetPharmacyName(pharmacyName)
                .targetLatitude(latitude)
                .targetLongitude(longitude)
                .build();
        
        String expectedUrl = UriComponentsBuilder.fromUriString(DIRECTION_BASE_URL + String.join(",", 
                pharmacyName, String.valueOf(latitude), String.valueOf(longitude)))
                .toUriString();
        
        // Mock 설정
        when(base62Service.decodeDirectionId(encodedId)).thenReturn(decodedId);
        when(directionRepository.findById(decodedId)).thenReturn(Optional.of(direction));
        
        // when
        String result = directionService.findDirectionUrlById(encodedId);
        log.info("result: {}", result);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedUrl);
    }
}