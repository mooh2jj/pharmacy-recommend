package com.dsg.pharmacyrecommend.domain.pharmacy.repository;

import com.dsg.pharmacyrecommend.domain.pharmacy.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
}
