package com.dsg.pharmacyrecommend.pharmacy.service;

import com.dsg.pharmacyrecommend.domain.pharmacy.entity.Pharmacy;
import com.dsg.pharmacyrecommend.domain.pharmacy.repository.PharmacyRepository;
import com.dsg.pharmacyrecommend.domain.pharmacy.service.PharmacyRepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
class PharmacyTransactionTest {

    @Autowired
    private PharmacyRepositoryService pharmacyRepositoryService;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    private Pharmacy pharmacy;

    @BeforeEach
    void setup() {
        // 테스트 데이터 초기화
        pharmacyRepository.deleteAll();
        
        pharmacy = Pharmacy.builder()
                .pharmacyName("테스트 약국")
                .pharmacyAddress("서울시 강남구")
                .latitude(37.1234)
                .longitude(127.1234)
                .build();
    }

    @Test
    @DisplayName("내부 호출시 @Transactional이 적용되지 않아 데이터가 저장된다")
    void whenInternalCall_thenTransactionalNotApplied() {
        // given
        List<Pharmacy> pharmacyList = List.of(pharmacy);

        // when & then
        assertThatThrownBy(() -> pharmacyRepositoryService.bar(pharmacyList))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // then
        List<Pharmacy> savedPharmacies = pharmacyRepository.findAll();
        assertThat(savedPharmacies).hasSize(1);
        assertThat(savedPharmacies.get(0).getPharmacyName()).isEqualTo("테스트 약국");
    }

    @Test
    @DisplayName("외부 호출시 @Transactional이 적용되어 롤백된다")
    void whenExternalCall_thenTransactionalApplied() {
        // given
        List<Pharmacy> pharmacyList = List.of(pharmacy);

        // when & then
        assertThatThrownBy(() -> pharmacyRepositoryService.foo(pharmacyList))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // then
        List<Pharmacy> savedPharmacies = pharmacyRepository.findAll();
        assertThat(savedPharmacies).isEmpty();
    }

    @Test
    @DisplayName("상위 트랜잭션이 존재할 경우 해당 트랜잭션에 참여한다")
    @Transactional
    void whenParentTransactionExists_thenParticipateInTransaction() {
        // given
        List<Pharmacy> pharmacyList = List.of(pharmacy);

        // when & then
        assertThatThrownBy(() -> pharmacyRepositoryService.foo(pharmacyList))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // then
        List<Pharmacy> savedPharmacies = pharmacyRepository.findAll();
        log.info("savedPharmacies: {}", savedPharmacies);
        assertThat(savedPharmacies).hasSize(1);
    }
} 