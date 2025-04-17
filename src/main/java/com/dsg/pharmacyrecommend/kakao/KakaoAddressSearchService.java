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

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAddressSearchService {

    private final RestTemplate restTemplate;
    private final KakaoUriBuilderService kakaoUriBuilderService;

    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    /**
     * 카카오 주소 검색 API 호출
     * @param address 검색할 주소
     * @return KakaoApiResponseDto : 주소 검색 결과
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 2000)
    )
    public KakaoApiResponseDto requestAddressSearch(String address) {
        // kakao api 호출
        URI uri = kakaoUriBuilderService.buildUriByAddressSearch(address);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<KakaoApiResponseDto> response = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, KakaoApiResponseDto.class);
        log.info("[KakaoApiAddressSearchService requestAddressSearch] response: {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        return null;
    }

    /**
     * 재시도 후에도 실패한 경우 호출되는 메서드
     * @param e 예외
     * @param address 주소
     * @return null
     */
    @Recover
    public KakaoApiResponseDto recover(RuntimeException e, String address) {
        log.error("[KakaoApiAddressSearchService recover] error: {}, address: {}", e.getMessage(), address);
        return null;
    }
}
