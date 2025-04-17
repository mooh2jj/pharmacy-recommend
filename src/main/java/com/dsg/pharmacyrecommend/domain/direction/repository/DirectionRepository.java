package com.dsg.pharmacyrecommend.domain.direction.repository;

import com.dsg.pharmacyrecommend.domain.direction.entity.Direction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectionRepository extends JpaRepository<Direction, Long> {
}
