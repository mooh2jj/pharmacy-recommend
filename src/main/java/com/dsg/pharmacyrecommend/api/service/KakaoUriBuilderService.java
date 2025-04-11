package com.dsg.pharmacyrecommend.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
public class KakaoUriBuilderService {

    private static final String KAKAO_LOCAL_SEARCH_ADDRESS_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    private static final String KAKAO_LOCAL_CATEGORY_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/category.json";

    /**
     * 카카오 주소 검색 API 호출을 위한 URI 빌드
     * @param address 검색할 주소
     * @return URI : 카카오 주소 검색 API URI
     */
    public URI buildUriByAddressSearch(String address) {
        // 테스트용 URL이 설정되어 있는지 확인
        String baseUrl = System.getProperty("kakao.rest.api.url");
        log.info("baseUrl: {}", baseUrl);

        UriComponentsBuilder uriBuilder;
        if (baseUrl != null) {
            // 테스트용 URL 사용
            uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + "v2/local/search/address.json");
        } else {
            // 실제 카카오 API URL 사용
            uriBuilder = UriComponentsBuilder.fromHttpUrl(KAKAO_LOCAL_SEARCH_ADDRESS_URL);
        }

        uriBuilder.queryParam("query", address);

        URI uri = uriBuilder.build().encode().toUri(); // encode default utf-8
        log.info("[KakaoAddressSearchService buildUriByAddressSearch] address: {}, uri: {}", address, uri);

        return uri;
    }

    public URI buildUriByCategorySearch(double latitude, double longitude, double radius, String category) {

        double meterRadius = radius * 1000;

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(KAKAO_LOCAL_CATEGORY_SEARCH_URL);
        uriBuilder.queryParam("category_group_code", category);
        uriBuilder.queryParam("x", longitude);
        uriBuilder.queryParam("y", latitude);
        uriBuilder.queryParam("radius", meterRadius);
        uriBuilder.queryParam("sort","distance");

        URI uri = uriBuilder.build().encode().toUri();

        log.info("[KakaoAddressSearchService buildUriByCategorySearch] uri: {} ", uri);

        return uri;
    }
}
