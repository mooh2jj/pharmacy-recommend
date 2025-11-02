package com.dsg.pharmacyrecommend.kakao;

import com.dsg.pharmacyrecommend.kakao.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * 카카오 카테고리 검색 API를 호출하는 서비스 클래스
 * 
 * 이 클래스는 특정 위치(위도, 경도) 기준으로 반경 내의 약국을 검색하는 기능을 제공합니다.
 * 카카오 지도의 카테고리 검색 API를 활용하여 실시간으로 약국 정보를 조회할 수 있습니다.
 * 
 * 주요 기능:
 * - 위치 기반 약국 카테고리 검색
 * - 반경 내 약국 정보 조회 (거리순 정렬)
 * - 실시간 약국 정보 제공 (카카오 지도 데이터 기반)
 * 
 * 사용 시나리오:
 * - 기존 DB에 약국 정보가 부족할 때
 * - 실시간 약국 정보가 필요할 때
 * - 새로운 약국이나 폐점 정보를 반영해야 할 때
 * 
 * @author dsg
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoCategorySearchService {

    /**
     * 카카오 API URI를 구성하는 서비스
     */
    private final KakaoUriBuilderService kakaoUriBuilderService;

    /**
     * HTTP 요청을 처리하는 RestTemplate
     */
    private final RestTemplate restTemplate;

    /**
     * 카카오 지도 카테고리 코드 - 약국 (PM9)
     * 
     * 카카오 지도에서 정의한 장소 카테고리 코드:
     * PM9: 약국
     * HP8: 병원
     * MT1: 마트
     * 등등... 각 카테고리마다 고유한 코드가 있음
     */
    private static final String PHARMACY_CATEGORY = "PM9";

    /**
     * 카카오 REST API 키 (application.yml에서 주입)
     */
    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    /**
     * 특정 위치 기준 반경 내 약국을 카테고리 검색으로 조회합니다.
     * 
     * 이 메서드는 사용자의 현재 위치를 기준으로 지정된 반경 내에 있는
     * 모든 약국 정보를 카카오 지도 API를 통해 실시간으로 조회합니다.
     * 
     * 검색 특징:
     * - 거리순 정렬 (가까운 순서대로)
     * - 실시간 약국 정보 (영업시간, 전화번호 등 포함)
     * - 카카오 지도에서 제공하는 최신 데이터 활용
     * 
     * 활용 장면:
     * - 자체 DB의 약국 정보가 부족한 경우
     * - 실시간 약국 상태 확인이 필요한 경우
     * - 새로 개업했거나 폐점한 약국 정보 반영
     * 
     * @param latitude 검색 기준 위도 (WGS84 좌표계)
     * @param longitude 검색 기준 경도 (WGS84 좌표계)
     * @param radius 검색 반경 (킬로미터 단위, 최대 20km)
     * @return KakaoApiResponseDto 검색된 약국 목록 정보
     *         - 약국명, 주소, 전화번호, 위치좌표, 거리 등 포함
     *         - 거리순으로 정렬된 결과
     */
    public KakaoApiResponseDto requestPharmacyCategorySearch(double latitude, double longitude, double radius) {

        // 카카오 카테고리 검색 API URI 생성
        URI uri = kakaoUriBuilderService.buildUriByCategorySearch(latitude, longitude, radius, PHARMACY_CATEGORY);

        // HTTP 헤더 설정 (카카오 API 인증키 포함)
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        log.info("[KakaoCategorySearchService] 약국 카테고리 검색 요청 - " +
                "위도: {}, 경도: {}, 반경: {}km", latitude, longitude, radius);

        // 카카오 카테고리 검색 API 호출 및 결과 반환
        return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, KakaoApiResponseDto.class).getBody();
    }
}
