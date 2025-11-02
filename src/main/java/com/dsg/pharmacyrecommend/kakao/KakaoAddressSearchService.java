package com.dsg.pharmacyrecommend.kakao;

import com.dsg.pharmacyrecommend.kakao.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * 카카오 주소 검색 API를 호출하는 서비스 클래스
 * 
 * 이 클래스는 사용자가 입력한 주소를 카카오 지도 API를 통해 검색하여
 * 위도, 경도 정보를 포함한 상세 주소 정보를 조회하는 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 카카오 주소 검색 API 호출
 * - 네트워크 오류 시 재시도 기능 (최대 2회)
 * - API 응답 실패 시 복구 메커니즘
 * 
 * @author dsg
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAddressSearchService {

    /**
     * HTTP 요청을 처리하는 RestTemplate
     */
    private final RestTemplate restTemplate;
    
    /**
     * 카카오 API URI를 구성하는 서비스
     */
    private final KakaoUriBuilderService kakaoUriBuilderService;

    /**
     * 카카오 REST API 키 (application.yml에서 주입)
     */
    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    /**
     * 카카오 주소 검색 API를 호출하여 주소 정보를 조회합니다.
     * 
     * 이 메서드는 사용자가 입력한 주소를 카카오 지도 API로 전송하여
     * 해당 주소의 위도, 경도를 포함한 상세 정보를 조회합니다.
     * 
     * 재시도 정책:
     * - 최대 2회 재시도 (총 3회 호출 가능)
     * - 재시도 간격: 2초
     * - RuntimeException 발생 시 재시도
     * 
     * @param address 검색할 주소 (예: "서울특별시 강남구 테헤란로 142")
     * @return KakaoApiResponseDto 주소 검색 결과 (위도, 경도, 정확한 주소명 포함)
     *         API 호출 실패 시 null 반환
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 2000)
    )
    public KakaoApiResponseDto requestAddressSearch(String address) {
        // 카카오 주소 검색 API URI 생성
        URI uri = kakaoUriBuilderService.buildUriByAddressSearch(address);

        // HTTP 헤더 설정 (카카오 API 인증키 포함)
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        // 카카오 API 호출 및 응답 처리
        ResponseEntity<KakaoApiResponseDto> response = restTemplate.exchange(
            uri, HttpMethod.GET, httpEntity, KakaoApiResponseDto.class
        );
        log.info("[KakaoApiAddressSearchService requestAddressSearch] address: {}, response status: {}", 
                address, response.getStatusCode());

        // 성공 응답인 경우 결과 반환, 실패 시 null 반환
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        
        log.warn("[KakaoApiAddressSearchService requestAddressSearch] API 호출 실패 - status: {}", 
                response.getStatusCode());
        return null;
    }

    /**
     * 재시도가 모두 실패했을 때 호출되는 복구(Recovery) 메서드
     * 
     * @Retryable 메서드가 모든 재시도를 실패한 경우 자동으로 호출됩니다.
     * 이 메서드는 최종적으로 API 호출이 실패했을 때의 처리 로직을 담당합니다.
     * 
     * 복구 시나리오:
     * 1. 네트워크 연결 실패
     * 2. 카카오 API 서버 오류 (5xx 에러)
     * 3. API 응답 타임아웃
     * 4. 잘못된 API 키 또는 권한 문제
     * 
     * @param e 발생한 예외 정보
     * @param address 검색하려던 주소
     * @return null (복구 불가능한 상황이므로 null 반환)
     */
    @Recover
    public KakaoApiResponseDto recover(RuntimeException e, String address) {
        log.error("[KakaoApiAddressSearchService recover] 카카오 주소 검색 API 호출 최종 실패 - " +
                "error: {}, address: {}, 서비스 이용에 문제가 있을 수 있습니다.", 
                e.getMessage(), address);
        return null;
    }
}
