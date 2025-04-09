package com.dsg.pharmacyrecommend.service;

import com.dsg.pharmacyrecommend.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiAddressSearchService {

    private final RestTemplate restTemplate;
    private final KakaoUriBuilderService kakaoUriBuilderService;

    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    /**
     * 카카오 주소 검색 API 호출
     * @param address 검색할 주소
     * @return KakaoApiResponseDto : 주소 검색 결과
     */
    public KakaoApiResponseDto requestAddressSearch(String address) {
        // kakao api 호출
        URI uri = kakaoUriBuilderService.buildUriByAddressSearch(address);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);
        HttpEntity httpEntity = new HttpEntity<>(headers);

        ResponseEntity<KakaoApiResponseDto> response = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, KakaoApiResponseDto.class);
        log.info("[KakaoApiAddressSearchService requestAddressSearch] response: {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        return null;
    }
}
