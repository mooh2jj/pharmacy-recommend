package com.dsg.pharmacyrecommend.service;

import com.dsg.pharmacyrecommend.dto.DocumentDto;
import com.dsg.pharmacyrecommend.dto.KakaoApiResponseDto;
import com.dsg.pharmacyrecommend.dto.MetaDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class KakaoApiAddressSearchServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KakaoUriBuilderService kakaoUriBuilderService;

    @InjectMocks
    private KakaoApiAddressSearchService kakaoApiAddressSearchService;

    @Test
    @DisplayName("정상적인 주소 검색 요청시 정상적으로 응답이 반환된다")
    void requestAddressSearchReturnSuccessfully() {
        // given
        String address = "서울 성북구";
//        URI mockUri = URI.create("https://dapi.kakao.com/v2/local/search/address.json?query=" + address);
        URI mockUri = UriComponentsBuilder // 한글 인코딩을 위한 처리
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", address)
                .build()
                .encode()
                .toUri();

        DocumentDto documentDto = DocumentDto.builder()
                .addressName("서울 성북구")
                .longitude(127.0)
                .latitude(37.0)
                .distance(0.0)
                .build();

        List<DocumentDto> documentList = new ArrayList<>();
        documentList.add(documentDto);

        MetaDto metaDto = new MetaDto(1);
        KakaoApiResponseDto expectedResponse = new KakaoApiResponseDto(documentList, metaDto);
        
        given(kakaoUriBuilderService.buildUriByAddressSearch(address))
                .willReturn(mockUri);
        
        given(restTemplate.exchange(
                eq(mockUri),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoApiResponseDto.class)
        )).willReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // when
        KakaoApiResponseDto result = kakaoApiAddressSearchService.requestAddressSearch(address);

        log.info("result: {}", result);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getDocumentList()).hasSize(1);
        assertThat(result.getMetaDto().getTotalCount()).isEqualTo(1);
        assertThat(result.getDocumentList().get(0).getAddressName()).isEqualTo("서울 성북구");
        assertThat(result.getDocumentList().get(0).getLongitude()).isEqualTo(127.0);
        assertThat(result.getDocumentList().get(0).getLatitude()).isEqualTo(37.0);

        // verify
        verify(kakaoUriBuilderService).buildUriByAddressSearch(address);
        verify(restTemplate).exchange(
                eq(mockUri),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoApiResponseDto.class)
        );
    }

    @Test
    @DisplayName("주소 검색 실패시 null을 반환한다")
    void requestAddressSearchReturnNullWhenFailed() {
        // given
        String address = "서울 성북구";
        URI mockUri = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", address)
                .build()
                .encode()
                .toUri();
        
        given(kakaoUriBuilderService.buildUriByAddressSearch(address))
                .willReturn(mockUri);
        
        given(restTemplate.exchange(
                eq(mockUri),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoApiResponseDto.class)
        )).willReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        // when
        KakaoApiResponseDto result = kakaoApiAddressSearchService.requestAddressSearch(address);

        // then
        assertThat(result).isNull();

        // verify
        verify(kakaoUriBuilderService).buildUriByAddressSearch(address);
        verify(restTemplate).exchange(
                eq(mockUri),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoApiResponseDto.class)
        );
    }
} 