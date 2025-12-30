package com.korconstructions.repository;

import com.korconstructions.model.Building;
import com.korconstructions.model.BuildingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByStatus(BuildingStatus status);
    List<Building> findByCustomerId(Long customerId);
}
