package com.dsg.pharmacyrecommend.pharmacy.service;

import com.dsg.pharmacyrecommend.pharmacy.entity.Pharmacy;
import com.dsg.pharmacyrecommend.pharmacy.repository.PharmacyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PharmacyRepositoryServiceTest {

    @Mock
    private PharmacyRepository pharmacyRepository;

    @InjectMocks
    private PharmacyRepositoryService pharmacyRepositoryService;

    private Pharmacy pharmacy1;
    private Pharmacy pharmacy2;

    @BeforeEach
    void setUp() {
        pharmacy1 = Pharmacy.builder()
                .id(1L)
                .pharmacyName("약국1")
                .pharmacyAddress("서울시 강남구")
                .latitude(37.1234)
                .longitude(127.1234)
                .build();

        pharmacy2 = Pharmacy.builder()
                .id(2L)
                .pharmacyName("약국2")
                .pharmacyAddress("서울시 서초구")
                .latitude(37.5678)
                .longitude(127.5678)
                .build();
    }

    @Test
    @DisplayName("약국 목록을 저장하고 반환한다")
    void saveAll() {
        // given
        List<Pharmacy> pharmacyList = Arrays.asList(pharmacy1, pharmacy2);
        given(pharmacyRepository.saveAll(any())).willReturn(pharmacyList);

        // when
        List<Pharmacy> result = pharmacyRepositoryService.saveAll(pharmacyList);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPharmacyName()).isEqualTo("약국1");
        assertThat(result.get(1).getPharmacyName()).isEqualTo("약국2");
        verify(pharmacyRepository).saveAll(pharmacyList);
    }

    @Test
    @DisplayName("빈 약국 목록을 저장할 경우 빈 리스트를 반환한다")
    void saveAllEmptyList() {
        // given
        List<Pharmacy> emptyList = List.of();

        // when
        List<Pharmacy> result = pharmacyRepositoryService.saveAll(emptyList);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("약국 주소를 업데이트한다")
    void updateAddress() {
        // given
        String newAddress = "서울시 송파구";
        given(pharmacyRepository.findById(1L)).willReturn(Optional.of(pharmacy1));

        // when
        pharmacyRepositoryService.updateAddress(1L, newAddress);

        // then
        assertThat(pharmacy1.getPharmacyAddress()).isEqualTo(newAddress);
        verify(pharmacyRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 약국 ID로 주소 업데이트 시도 시 로그를 남긴다")
    void updateAddressNotFound() {
        // given
        given(pharmacyRepository.findById(999L)).willReturn(Optional.empty());

        // when
        pharmacyRepositoryService.updateAddress(999L, "새 주소");

        // then
        verify(pharmacyRepository).findById(999L);
    }

    @Test
    @DisplayName("모든 약국 목록을 조회한다")
    void findAll() {
        // given
        List<Pharmacy> pharmacyList = Arrays.asList(pharmacy1, pharmacy2);
        given(pharmacyRepository.findAll()).willReturn(pharmacyList);

        // when
        List<Pharmacy> result = pharmacyRepositoryService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPharmacyName()).isEqualTo("약국1");
        assertThat(result.get(1).getPharmacyName()).isEqualTo("약국2");
        verify(pharmacyRepository).findAll();
    }
} 