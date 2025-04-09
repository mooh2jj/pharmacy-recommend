package com.dsg.pharmacyrecommend.pharmacy.repository;

import com.dsg.pharmacyrecommend.pharmacy.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
}
