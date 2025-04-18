package com.dsg.pharmacyrecommend.domain.pharmacy.cache;

import com.dsg.pharmacyrecommend.domain.pharmacy.dto.PharmacyDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRedisTemplateService {

    private static final String CACHE_KEY = "PHARMACY";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * RedisTemplate의 HashOperations을 사용하기 위한 초기화 메서드
     */
    private HashOperations<String, String, String> hashOperations;

    @PostConstruct
    public void init() {
        /**
         * Hash 자료구조 사용
         */
        this.hashOperations = redisTemplate.opsForHash();
    }

    /**
     * 약국 정보를 Redis에 저장
     * @param pharmacyDto 약국 DTO
     */
    public void save(PharmacyDto pharmacyDto) {
        if (Objects.isNull(pharmacyDto) || Objects.isNull(pharmacyDto.getId())) {
            log.error("Required Values must not be null");
            return;
        }
        try {
            hashOperations.put(CACHE_KEY,
                    pharmacyDto.getId().toString(),
                    serializePharmacyDto(pharmacyDto));
            log.info("[PharmacyRedisTemplateService save success] id: {}", pharmacyDto.getId());
        } catch (Exception e) {
            log.error("[PharmacyRedisTemplateService save error] {}", e.getMessage());
        }
    }

    /**
     * 약국 정보를 Redis에서 조회
     * @return 약국 DTO 리스트
     */
    public List<PharmacyDto> findAll() {

        try {
            List<PharmacyDto> list = new ArrayList<>();
            for (String value : hashOperations.entries(CACHE_KEY).values()) {
                PharmacyDto pharmacyDto = deserializePharmacyDto(value);
                list.add(pharmacyDto);
            }
            return list;

        } catch (Exception e) {
            log.error("[PharmacyRedisTemplateService findAll error]: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 약국 정보를 Redis에서 조회
     * @param id 약국 ID
     */
    public void delete(Long id) {
        hashOperations.delete(CACHE_KEY, String.valueOf(id));
        log.info("[PharmacyRedisTemplateService delete]: {} ", id);
    }

    /**
     * PharmacyDto를 JSON 문자열로 변환
     * @param pharmacyDto 약국 DTO
     * @return JSON 문자열
     * @throws JsonProcessingException 변환 예외
     */
    private String serializePharmacyDto(PharmacyDto pharmacyDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(pharmacyDto);
    }

    /**
     * JSON 문자열을 PharmacyDto로 변환
     * @param value JSON 문자열
     * @return 약국 DTO
     * @throws JsonProcessingException 변환 예외
     */
    private PharmacyDto deserializePharmacyDto(String value) throws JsonProcessingException {
        return objectMapper.readValue(value, PharmacyDto.class);
    }
}
