package com.dsg.pharmacyrecommend.domain.pharmacy.service;

import com.dsg.pharmacyrecommend.kakao.KakaoAddressSearchService;
import com.dsg.pharmacyrecommend.domain.direction.dto.OutputDto;
import com.dsg.pharmacyrecommend.domain.direction.entity.Direction;
import com.dsg.pharmacyrecommend.domain.direction.service.Base62Service;
import com.dsg.pharmacyrecommend.domain.direction.service.DirectionService;
import com.dsg.pharmacyrecommend.kakao.dto.DocumentDto;
import com.dsg.pharmacyrecommend.kakao.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 약국 추천 서비스의 핵심 비즈니스 로직을 담당하는 클래스
 * 
 * 이 서비스는 사용자가 입력한 주소를 기반으로 가까운 약국을 추천하는 
 * 전체 프로세스를 조정하고 관리합니다.
 * 
 * 주요 처리 과정:
 * 1. 입력 주소 → 카카오 API로 좌표 변환
 * 2. 좌표 기준 → 반경 내 약국 검색
 * 3. 거리 계산 → 가까운 순으로 정렬
 * 4. 결과 가공 → 프론트엔드용 DTO 변환
 * 5. URL 생성 → 길찾기, 로드뷰 링크 제공
 * 
 * 추천 전략:
 * - 최대 3개 약국 추천
 * - 직선거리 10km 이내 제한
 * - Haversine 공식을 통한 정확한 거리 계산
 * - Base62 인코딩을 통한 짧은 URL 생성
 * 
 * @author dsg
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    /**
     * 카카오맵 로드뷰 링크 생성을 위한 기본 URL
     * 
     * 사용법: ROAD_VIEW_BASE_URL + 위도 + "," + 경도
     * 예시: "https://map.kakao.com/link/roadview/37.5665,126.9780"
     */
    private static final String ROAD_VIEW_BASE_URL = "https://map.kakao.com/link/roadview/";

    /**
     * 카카오 주소 검색 서비스 - 주소를 좌표로 변환
     */
    private final KakaoAddressSearchService kakaoAddressSearchService;
    
    /**
     * 약국 방향 서비스 - 거리 계산 및 추천 로직
     */
    private final DirectionService directionService;
    
    /**
     * Base62 인코딩 서비스 - 짧은 URL 생성
     */
    private final Base62Service base62Service;

    /**
     * 약국 추천 서비스의 기본 URL (application.yml에서 설정)
     * 
     * 길찾기 단축 URL 생성에 사용됩니다.
     * 예시: "http://localhost:8085/dir/"
     */
    @Value("${pharmacy.recommendation.base.url}")
    private String baseUrl;

    /**
     * 주소 기반 약국 추천 서비스의 메인 메서드
     * 
     * 이 메서드는 사용자가 입력한 주소를 기반으로 가까운 약국을 찾아 추천하는
     * 전체 프로세스를 관리합니다.
     * 
     * 처리 단계:
     * 1. 주소 검증 및 좌표 변환
     * 2. 약국 데이터 조회 및 거리 계산
     * 3. 추천 결과 저장
     * 4. 프론트엔드용 DTO 변환
     * 
     * 추천 알고리즘:
     * - 현재: 자체 DB 기반 + Haversine 거리 계산 (더 정확한 결과)
     * - 대안: 카카오 카테고리 API 기반 (실시간 정보, 주석 처리됨)
     * 
     * 예외 처리:
     * - 잘못된 주소 입력 시 빈 리스트 반환
     * - API 호출 실패 시 빈 리스트 반환
     * - 검색 결과 없을 시 빈 리스트 반환
     * 
     * @param address 사용자가 입력한 검색 주소
     *                (예: "서울특별시 강남구 테헤란로 142")
     * @return List<OutputDto> 추천 약국 목록 (최대 3개)
     *         각 약국의 이름, 주소, 거리, 길찾기 URL, 로드뷰 URL 포함
     */
    public List<OutputDto> recommendPharmacyList(String address) {

        // 1단계: 입력 주소를 카카오 API를 통해 좌표로 변환
        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        // 주소 검색 실패 또는 결과 없음 체크
        if (Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentList())) {
            log.error("[PharmacyRecommendationService.recommendPharmacyList] " +
                    "주소 검색 실패 또는 결과 없음 - 입력 주소: {}", address);
            return Collections.emptyList();
        }

        // 첫 번째 검색 결과 사용 (가장 정확한 매칭 결과)
        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);
        log.info("[PharmacyRecommendationService.recommendPharmacyList] " +
                "주소 변환 완료 - 입력: {}, 결과: {}", address, documentDto);

        // 2단계: 약국 추천 로직 선택
        // 방법1: 자체 DB 약국 데이터 + 거리 계산 알고리즘 (현재 사용)
        List<Direction> directionList = directionService.buildDirectionList(documentDto);
        
        // 방법2: 카카오 카테고리 검색 API 사용 (실시간 약국 정보)
        // 더 최신 정보를 원할 경우 아래 주석을 해제하고 위 라인을 주석 처리
        // List<Direction> directionList = directionService.buildDirectionListByCategoryApi(documentDto);

        log.info("[PharmacyRecommendationService.recommendPharmacyList] " +
                "약국 검색 완료 - 검색된 약국 수: {}", directionList.size());

        // 3단계: 검색 결과 저장 및 프론트엔드용 DTO 변환
        return directionService.saveAll(directionList)
                .stream()
                .map(this::convertToOutputDto)
                .collect(Collectors.toList());
    }

    /**
     * Direction 엔티티를 프론트엔드용 OutputDto로 변환하는 메서드
     * 
     * 이 메서드는 데이터베이스에 저장된 Direction 정보를 클라이언트에서
     * 사용할 수 있는 형태의 DTO로 변환합니다.
     * 
     * 변환되는 정보:
     * - 약국명: 추천 약국의 이름
     * - 약국주소: 추천 약국의 전체 주소
     * - 길찾기URL: Base62 인코딩된 짧은 URL (클릭 시 카카오맵 연결)
     * - 로드뷰URL: 약국 위치의 거리뷰를 보여주는 카카오맵 링크
     * - 거리정보: "XX.XX km" 형태로 포맷된 거리 문자열
     * 
     * URL 생성 방식:
     * - 길찾기: 서비스 기본 URL + Base62로 인코딩된 Direction ID
     * - 로드뷰: 카카오맵 로드뷰 기본 URL + 위도,경도 좌표
     * 
     * @param direction 데이터베이스에서 조회한 약국 방향 정보
     * @return OutputDto 프론트엔드에서 사용할 약국 추천 결과 DTO
     */
    private OutputDto convertToOutputDto(Direction direction) {

        return OutputDto.builder()
                .pharmacyName(direction.getTargetPharmacyName())        // 약국명
                .pharmacyAddress(direction.getTargetAddress())          // 약국 주소
                // 길찾기 단축 URL 생성 (Base62 인코딩으로 URL 길이 최소화)
                .directionUrl(baseUrl + base62Service.encodeDirectionId(direction.getId()))
                // 로드뷰 URL 생성 (카카오맵 거리뷰 직접 연결)
                .roadViewUrl(ROAD_VIEW_BASE_URL + direction.getTargetLatitude() + "," + direction.getTargetLongitude())
                // 거리 정보를 소수점 둘째자리까지 표시
                .distance(String.format("%.2f km", direction.getDistance()))
                .build();
    }
}
