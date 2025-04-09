package com.dsg.pharmacyrecommend.direction.repository;

import com.dsg.pharmacyrecommend.direction.entity.Direction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectionRepository extends JpaRepository<Direction, Long> {
}
