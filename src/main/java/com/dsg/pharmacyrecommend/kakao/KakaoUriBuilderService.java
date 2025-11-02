package com.dsg.pharmacyrecommend.kakao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * 카카오 지도 API 호출을 위한 URI 생성 서비스 클래스
 * 
 * 이 클래스는 카카오 지도의 다양한 API 엔드포인트에 대한 URI를 구성하는 역할을 담당합니다.
 * 각 API의 요구사항에 맞게 쿼리 파라미터를 설정하고, 올바른 형태의 URI를 생성합니다.
 * 
 * 지원하는 API:
 * 1. 주소 검색 API - 주소를 좌표로 변환
 * 2. 카테고리 검색 API - 특정 카테고리의 장소 검색
 * 
 * URI 구성 시 고려사항:
 * - 한글 주소의 UTF-8 인코딩 처리
 * - 좌표값의 정확성 유지
 * - 쿼리 파라미터의 순서와 형식
 * - 테스트 환경과 운영 환경의 URL 분리
 * 
 * @author dsg
 * @version 1.0
 */
@Slf4j
@Service
public class KakaoUriBuilderService {

    /**
     * 카카오 주소 검색 API 엔드포인트
     * 
     * 이 API는 주소 문자열을 입력받아 해당 위치의 좌표(위도, 경도)와 
     * 정확한 주소 정보를 반환합니다.
     */
    private static final String KAKAO_LOCAL_SEARCH_ADDRESS_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    /**
     * 카카오 카테고리 검색 API 엔드포인트
     * 
     * 이 API는 특정 위치 기준으로 반경 내의 특정 카테고리(약국, 병원 등)에 
     * 해당하는 장소들을 검색하여 반환합니다.
     */
    private static final String KAKAO_LOCAL_CATEGORY_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/category.json";

    /**
     * 카카오 주소 검색 API 호출을 위한 URI를 구성합니다.
     * 
     * 이 메서드는 사용자가 입력한 주소 문자열을 카카오 주소 검색 API에서
     * 처리할 수 있는 형태의 URI로 변환합니다.
     * 
     * URI 구성 과정:
     * 1. 테스트 환경 여부 확인 (시스템 프로퍼티 체크)
     * 2. 기본 URL 설정 (테스트용 또는 운영용)
     * 3. 쿼리 파라미터 추가 (주소 문자열)
     * 4. UTF-8 인코딩 처리 (한글 주소 지원)
     * 
     * 테스트 지원:
     * - 시스템 프로퍼티 'kakao.rest.api.url'이 설정되면 테스트 서버 사용
     * - Mock 서버나 개발 서버를 활용한 테스트 가능
     * 
     * @param address 검색하고자 하는 주소 문자열 
     *                (예: "서울특별시 강남구 테헤란로 142", "경기도 성남시 분당구 백현동 555")
     * @return URI 카카오 주소 검색 API 호출을 위한 완전한 URI
     *             쿼리 파라미터와 인코딩이 모두 적용된 상태
     */
    public URI buildUriByAddressSearch(String address) {
        // 테스트용 URL이 시스템 프로퍼티로 설정되어 있는지 확인
        String baseUrl = System.getProperty("kakao.rest.api.url");
        log.info("[KakaoUriBuilderService] 사용할 기본 URL 확인 - baseUrl: {}", baseUrl);

        UriComponentsBuilder uriBuilder;
        if (baseUrl != null) {
            // 테스트 환경: Mock 서버나 개발 서버 URL 사용
            uriBuilder = UriComponentsBuilder.fromUriString(baseUrl + "v2/local/search/address.json");
            log.info("[KakaoUriBuilderService] 테스트 환경에서 API 호출");
        } else {
            // 운영 환경: 실제 카카오 API URL 사용
            uriBuilder = UriComponentsBuilder.fromUriString(KAKAO_LOCAL_SEARCH_ADDRESS_URL);
            log.info("[KakaoUriBuilderService] 운영 환경에서 API 호출");
        }

        // 검색 주소를 쿼리 파라미터로 추가
        uriBuilder.queryParam("query", address);

        // UTF-8 인코딩 적용 (한글 주소 처리를 위해 필수)
        URI uri = uriBuilder.build().encode().toUri();
        log.info("[KakaoUriBuilderService buildUriByAddressSearch] " +
                "요청 주소: {}, 생성된 URI: {}", address, uri);

        return uri;
    }

    /**
     * 카카오 카테고리 검색 API 호출을 위한 URI를 구성합니다.
     * 
     * 이 메서드는 특정 위치를 중심으로 지정된 반경 내에서 특정 카테고리의
     * 장소들을 검색하기 위한 URI를 생성합니다.
     * 
     * URI 구성 요소:
     * - category_group_code: 검색할 카테고리 코드 (PM9=약국, HP8=병원 등)
     * - x: 경도 (longitude) - 동서 방향 좌표
     * - y: 위도 (latitude) - 남북 방향 좌표
     * - radius: 검색 반경 (미터 단위)
     * - sort: 정렬 기준 (distance=거리순)
     * 
     * 좌표계 정보:
     * - WGS84 좌표계 사용
     * - 위도: -90 ~ 90 범위
     * - 경도: -180 ~ 180 범위
     * 
     * @param latitude 검색 중심점의 위도 (북위 양수, 남위 음수)
     * @param longitude 검색 중심점의 경도 (동경 양수, 서경 음수)
     * @param radius 검색 반경 (킬로미터 단위, 최대 20km)
     * @param category 카테고리 코드 (PM9: 약국, HP8: 병원, MT1: 마트 등)
     * @return URI 카카오 카테고리 검색 API 호출을 위한 완전한 URI
     *             모든 쿼리 파라미터가 적용된 상태
     */
    public URI buildUriByCategorySearch(double latitude, double longitude, double radius, String category) {

        // 반경을 킬로미터에서 미터로 변환 (카카오 API는 미터 단위 사용)
        double meterRadius = radius * 1000;

        // 기본 URL로부터 UriComponentsBuilder 생성
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(KAKAO_LOCAL_CATEGORY_SEARCH_URL);
        
        // 카테고리 검색에 필요한 쿼리 파라미터들 추가
        uriBuilder.queryParam("category_group_code", category); // 카테고리 코드
        uriBuilder.queryParam("x", longitude);                  // 경도 (x축)
        uriBuilder.queryParam("y", latitude);                   // 위도 (y축)
        uriBuilder.queryParam("radius", meterRadius);           // 검색 반경 (미터)
        uriBuilder.queryParam("sort", "distance");              // 거리순 정렬

        // URI 구성 완료 및 인코딩 적용
        URI uri = uriBuilder.build().encode().toUri();

        log.info("[KakaoUriBuilderService buildUriByCategorySearch] " +
                "카테고리 검색 URI 생성 완료 - 위도: {}, 경도: {}, 반경: {}km, 카테고리: {}, URI: {}", 
                latitude, longitude, radius, category, uri);

        return uri;
    }
}
