package com.dsg.pharmacyrecommend.domain.direction.service;

import com.dsg.pharmacyrecommend.kakao.KakaoCategorySearchService;
import com.dsg.pharmacyrecommend.domain.direction.entity.Direction;
import com.dsg.pharmacyrecommend.domain.direction.repository.DirectionRepository;
import com.dsg.pharmacyrecommend.kakao.dto.DocumentDto;
import com.dsg.pharmacyrecommend.domain.pharmacy.service.PharmacySearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 약국 방향 및 거리 계산 서비스 클래스
 * 
 * 이 클래스는 사용자의 위치와 약국들 간의 거리를 계산하고, 
 * 가장 가까운 약국들을 추천하는 핵심 알고리즘을 담당합니다.
 * 
 * 주요 기능:
 * 1. 거리 계산 - Haversine 공식을 활용한 정확한 구면 거리 계산
 * 2. 약국 필터링 - 반경 10km 이내 약국만 추천 대상으로 선별  
 * 3. 정렬 및 제한 - 거리순 정렬 후 최대 3개까지 추천
 * 4. URL 생성 - 길찾기를 위한 카카오맵 링크 생성
 * 5. 데이터 저장 - 추천 결과를 데이터베이스에 저장하여 추후 분석 가능
 * 
 * 거리 계산 방식:
 * - Haversine Formula 사용 (지구의 곡률을 고려한 구면 거리)
 * - WGS84 좌표계 기준
 * - 결과 정확도: ±0.5% 이내 (실제 거리 대비)
 * 
 * 추천 정책:
 * - 반경: 10km 이내
 * - 개수: 최대 3개
 * - 정렬: 가까운 거리 우선
 * 
 * @author dsg
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    /**
     * 추천할 최대 약국 개수 (UX 고려하여 3개로 제한)
     */
    private static final int MAX_SEARCH_COUNT = 3;
    
    /**
     * 약국 검색 반경 (10km 이내만 추천 대상으로 설정)
     * 도보 이동 가능성과 대중교통 접근성을 고려한 합리적 거리
     */
    private static final double RADIUS_KM = 10.0;
    
    /**
     * 카카오맵 길찾기 기본 URL
     * 
     * 사용법: DIRECTION_BASE_URL + 약국명 + "," + 위도 + "," + 경도
     */
    private static final String DIRECTION_BASE_URL = "https://map.kakao.com/link/map/";

    /**
     * 약국 검색 서비스 - 자체 DB에서 약국 정보 조회
     */
    private final PharmacySearchService pharmacySearchService;
    
    /**
     * 약국 방향 정보 저장소
     */
    private final DirectionRepository directionRepository;
    
    /**
     * Base62 인코딩 서비스 - URL 단축을 위한 ID 인코딩
     */
    private final Base62Service base62Service;

    /**
     * 카카오 카테고리 검색 서비스 - 실시간 약국 정보 조회용
     */
    private final KakaoCategorySearchService kakaoCategorySearchService;

    /**
     * 약국 방향 정보 리스트를 데이터베이스에 일괄 저장합니다.
     * 
     * 이 메서드는 추천된 약국들의 정보를 데이터베이스에 저장하여
     * 추후 통계 분석이나 사용자 추적에 활용할 수 있도록 합니다.
     * 
     * 저장되는 정보:
     * - 입력 주소 및 좌표
     * - 추천 약국 정보 (이름, 주소, 좌표)
     * - 계산된 거리
     * - 생성 시간
     * 
     * @param directionList 저장할 약국 방향 정보 리스트
     * @return List<Direction> 저장 완료된 Direction 엔티티 리스트 (ID 포함)
     *         입력 리스트가 비어있으면 빈 리스트 반환
     */
    @Transactional
    public List<Direction> saveAll(List<Direction> directionList) {
        if (CollectionUtils.isEmpty(directionList)) return Collections.emptyList();
        return directionRepository.saveAll(directionList);
    }

    /**
     * Base62로 인코딩된 ID를 통해 카카오맵 길찾기 URL을 생성합니다.
     * 
     * 이 메서드는 프론트엔드에서 전달받은 단축된 ID를 디코딩하여
     * 실제 약국 정보를 조회하고, 카카오맵 길찾기 링크를 생성합니다.
     * 
     * 처리 과정:
     * 1. Base62 인코딩된 ID를 원본 Long ID로 디코딩
     * 2. 데이터베이스에서 해당 Direction 정보 조회  
     * 3. 약국 정보를 이용해 카카오맵 URL 파라미터 구성
     * 4. 완전한 카카오맵 길찾기 URL 생성
     * 
     * 생성되는 URL 형태:
     * "https://map.kakao.com/link/map/약국명,위도,경도"
     * 
     * 사용 시나리오:
     * - 사용자가 추천 약국의 "길찾기" 버튼 클릭
     * - 단축 URL을 통한 약국 정보 접근
     * 
     * @param encodedId Base62로 인코딩된 Direction ID
     * @return String 카카오맵 길찾기 URL
     *         해당 ID의 Direction이 없으면 NullPointerException 발생
     */
    @Transactional(readOnly = true)
    public String findDirectionUrlById(String encodedId) {

        // Base62 인코딩된 ID를 원본 Long 값으로 디코딩
        Long decodedId = base62Service.decodeDirectionId(encodedId);
        
        // 데이터베이스에서 Direction 정보 조회
        Direction direction = directionRepository.findById(decodedId).orElse(null);

        assert direction != null;
        
        // 카카오맵 URL 파라미터 구성 (약국명,위도,경도)
        String params = String.join(",", 
                direction.getTargetPharmacyName(),
                String.valueOf(direction.getTargetLatitude()), 
                String.valueOf(direction.getTargetLongitude()));

        // 완전한 카카오맵 길찾기 URL 생성
        return UriComponentsBuilder.fromUriString(DIRECTION_BASE_URL + params)
                .toUriString();
    }

    /**
     * 자체 DB 기반으로 반경 10km 이내 약국을 검색하여 Direction 리스트를 생성합니다.
     * 
     * 이 메서드는 사용자의 위치 정보를 바탕으로 자체 데이터베이스에 저장된 
     * 모든 약국과의 거리를 계산하여 가장 가까운 약국들을 추천합니다.
     * 
     * 처리 흐름:
     * 1. 자체 DB에서 모든 약국 정보 조회
     * 2. 각 약국과 사용자 위치 간 거리 계산 (Haversine 공식)
     * 3. 10km 이내 약국만 필터링
     * 4. 거리순으로 정렬
     * 5. 최대 3개까지 제한
     * 
     * 장점:
     * - 빠른 응답 속도 (외부 API 호출 없음)
     * - 안정적인 서비스 (네트워크 의존성 최소화)
     * - 데이터 일관성 보장
     * 
     * 단점:
     * - 실시간 약국 정보 반영 어려움
     * - 새로 개업/폐업한 약국 정보 지연
     * 
     * @param documentDto 카카오 API로부터 변환된 사용자 위치 정보
     *                   (주소명, 위도, 경도 포함)
     * @return List<Direction> 거리순으로 정렬된 추천 약국 리스트 (최대 3개)
     *         입력이 null이면 빈 리스트 반환
     */
    public List<Direction> buildDirectionList(DocumentDto documentDto) {
        if(Objects.isNull(documentDto)) return Collections.emptyList();

        return pharmacySearchService.searchPharmacyDtoList()
                .stream()
                .map(pharmacyDto ->
                        Direction.builder()
                                // 사용자 입력 정보
                                .inputAddress(documentDto.getAddressName())
                                .inputLatitude(documentDto.getLatitude())
                                .inputLongitude(documentDto.getLongitude())
                                // 추천 약국 정보
                                .targetPharmacyName(pharmacyDto.getPharmacyName())
                                .targetAddress(pharmacyDto.getPharmacyAddress())
                                .targetLatitude(pharmacyDto.getLatitude())
                                .targetLongitude(pharmacyDto.getLongitude())
                                // Haversine 공식으로 정확한 거리 계산
                                .distance(calculateDistance(
                                        documentDto.getLatitude(), documentDto.getLongitude(),
                                        pharmacyDto.getLatitude(), pharmacyDto.getLongitude()))
                                .build())
                // 반경 10km 이내 약국만 선별
                .filter(direction -> direction.getDistance() <= RADIUS_KM)
                // 가까운 거리 순으로 정렬
                .sorted(Comparator.comparing(Direction::getDistance))
                // 최대 3개까지만 추천
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
    }

    /**
     * 카카오 카테고리 검색 API를 이용하여 실시간 약국 정보로 Direction 리스트를 생성합니다.
     * 
     * 이 메서드는 자체 DB 대신 카카오 지도의 실시간 약국 데이터를 활용하여
     * 더욱 정확하고 최신의 약국 정보를 제공합니다.
     * 
     * 처리 흐름:
     * 1. 카카오 카테고리 검색 API 호출 (약국 카테고리: PM9)
     * 2. 실시간 약국 데이터 수신 (이미 거리순 정렬됨)
     * 3. Direction 객체로 변환
     * 4. 최대 3개까지 제한
     * 
     * 장점:
     * - 실시간 약국 정보 반영
     * - 신규 개업/폐업 약국 즉시 반영
     * - 영업시간, 전화번호 등 상세 정보 제공
     * - 카카오에서 제공하는 정확한 거리 정보
     * 
     * 단점:
     * - 외부 API 의존성 (네트워크 오류 가능성)
     * - 응답 시간 증가 가능성
     * - API 호출 비용 발생
     * 
     * 사용 시나리오:
     * - 높은 정확성이 요구되는 경우
     * - 실시간 약국 상태가 중요한 경우
     * - 자체 DB의 데이터가 부족한 지역
     * 
     * @param inputDocumentDto 카카오 API로부터 변환된 사용자 위치 정보
     *                        (주소명, 위도, 경도 포함)
     * @return List<Direction> 카카오 API 기반 실시간 약국 추천 리스트 (최대 3개)
     *         입력이 null이면 빈 리스트 반환
     */
    public List<Direction> buildDirectionListByCategoryApi(DocumentDto inputDocumentDto) {
        if(Objects.isNull(inputDocumentDto)) return Collections.emptyList();

        return kakaoCategorySearchService
                // 카카오 카테고리 검색 API로 실시간 약국 정보 조회
                .requestPharmacyCategorySearch(inputDocumentDto.getLatitude(), inputDocumentDto.getLongitude(), RADIUS_KM)
                .getDocumentList()
                .stream()
                .map(resultDocumentDto ->
                        Direction.builder()
                                // 사용자 입력 정보
                                .inputAddress(inputDocumentDto.getAddressName())
                                .inputLatitude(inputDocumentDto.getLatitude())
                                .inputLongitude(inputDocumentDto.getLongitude())
                                // 카카오 API에서 제공하는 약국 정보
                                .targetPharmacyName(resultDocumentDto.getPlaceName())     // 약국명
                                .targetAddress(resultDocumentDto.getAddressName())       // 약국 주소
                                .targetLatitude(resultDocumentDto.getLatitude())         // 약국 위도
                                .targetLongitude(resultDocumentDto.getLongitude())       // 약국 경도
                                // 카카오에서 계산된 거리를 미터 → 킬로미터로 변환
                                .distance(resultDocumentDto.getDistance() * 0.001)
                                .build())
                // 카카오 API는 이미 거리순으로 정렬되어 있으므로 정렬 과정 생략
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
    }

    /**
     * Haversine 공식을 사용하여 두 지점 간의 구면 거리를 계산합니다.
     * 
     * 이 메서드는 지구의 곡률을 고려하여 두 위경도 좌표 간의 최단 거리를
     * 정확하게 계산하는 Haversine 공식을 구현합니다.
     * 
     * Haversine 공식 특징:
     * - 지구를 완전한 구로 가정 (실제로는 타원체이므로 약간의 오차 존재)
     * - 오차 범위: ±0.5% 이내 (중거리 계산 시)
     * - 계산 복잡도: O(1) - 매우 빠른 연산
     * 
     * 수학적 배경:
     * - 구면삼각법 기반의 거리 계산
     * - 대권거리(Great Circle Distance) 계산
     * - 위경도를 라디안으로 변환하여 계산
     * 
     * 활용 분야:
     * - GPS 네비게이션 시스템
     * - 위치 기반 서비스 (LBS)
     * - 지리 정보 시스템 (GIS)
     * 
     * 참고 자료: https://en.wikipedia.org/wiki/Haversine_formula
     * 
     * @param lat1 첫 번째 지점의 위도 (도 단위)
     * @param lon1 첫 번째 지점의 경도 (도 단위)  
     * @param lat2 두 번째 지점의 위도 (도 단위)
     * @param lon2 두 번째 지점의 경도 (도 단위)
     * @return double 두 지점 간의 거리 (킬로미터 단위)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 위경도를 라디안으로 변환 (수학 함수는 라디안 단위 사용)
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        // 지구 반지름 (킬로미터 단위)
        // 평균 반지름을 사용 (실제로는 극반지름과 적도반지름이 다름)
        double earthRadius = 6371;
        
        // Haversine 공식 적용
        // acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon1 - lon2))
        return earthRadius * Math.acos(
            Math.sin(lat1) * Math.sin(lat2) + 
            Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2)
        );
    }
}
