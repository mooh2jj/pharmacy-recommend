package com.dsg.pharmacyrecommend.service;

import com.dsg.pharmacyrecommend.api.service.KakaoApiAddressSearchService;
import com.dsg.pharmacyrecommend.api.service.KakaoUriBuilderService;
import com.dsg.pharmacyrecommend.dto.DocumentDto;
import com.dsg.pharmacyrecommend.dto.KakaoApiResponseDto;
import com.dsg.pharmacyrecommend.dto.MetaDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "kakao.rest.api.key=${KAKAO_REST_API_KEY}"
})
class KakaoApiAddressSearchServiceRetryTest {

    @Autowired
    private KakaoApiAddressSearchService kakaoApiAddressSearchService;

    @Autowired
    private KakaoUriBuilderService kakaoUriBuilderService;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        
        // MockWebServer의 baseUrl을 RestTemplate이 사용할 수 있도록 설정
        System.setProperty("kakao.rest.api.url", mockWebServer.url("/").toString());
    }

    @AfterEach
    void clean() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    @DisplayName("주소 검색 시 재시도 성공 테스트")
    void retrySuccessTest() {
        // given
        String address = "서울 성북구 종암로 10길";
        String expectedResult = "{\"documents\":[{\"address_name\":\"서울 성북구 종암로10길\",\"y\":37.5960650456809,\"x\":127.037033003036}],\"meta\":{\"total_count\":1}}";

        mockWebServer.enqueue(new MockResponse().setResponseCode(504)); // 첫 번째 요청 실패
        mockWebServer.enqueue(new MockResponse().setResponseCode(200) // 두 번째 요청 성공
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(expectedResult));

        // when
        KakaoApiResponseDto result = kakaoApiAddressSearchService.requestAddressSearch(address);
        log.info("[KakaoApiAddressSearchServiceRetryTest retrySuccessTest] result: {}", result);
        // then
        assertThat(result.getDocumentList().get(0).getAddressName()).isEqualTo("서울 성북구 종암로10길");
        assertThat(result.getDocumentList().get(0).getLatitude()).isEqualTo(37.5960650456809);
        assertThat(result.getDocumentList().get(0).getLongitude()).isEqualTo(127.037033003036);
    }

    @Test
    @DisplayName("주소 검색 시 재시도 모두 실패 테스트")
    void retryFailTest() {
        // given
        String address = "서울 성북구 종암로 10길";

        // 응답을 2번 모두 실패로 설정 (maxAttempts=2 설정과 일치)
        mockWebServer.enqueue(new MockResponse().setResponseCode(504));
        mockWebServer.enqueue(new MockResponse().setResponseCode(504));

        // when
        KakaoApiResponseDto result = kakaoApiAddressSearchService.requestAddressSearch(address);

        // then
        assertThat(result).isNull();
    }
} 