package com.dsg.pharmacyrecommend.test;

import com.dsg.pharmacyrecommend.dto.KakaoApiResponseDto;
import com.dsg.pharmacyrecommend.service.KakaoAddressSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/kakao")
@RequiredArgsConstructor
public class KakaoApiController {

    private final KakaoAddressSearchService kakaoAddressSearchService;

    @GetMapping("/address")
    public ResponseEntity<KakaoApiResponseDto> test(@RequestParam String search) {
        KakaoApiResponseDto responseDto = kakaoAddressSearchService.requestAddressSearch(search);
        return ResponseEntity.ok(responseDto);
    }

}
