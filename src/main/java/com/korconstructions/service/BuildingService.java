package com.korconstructions.service;

import com.korconstructions.model.Building;
import com.korconstructions.model.BuildingStatus;
import com.korconstructions.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;

    @Autowired
    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }

    public Optional<Building> getBuildingById(Long id) {
        return buildingRepository.findById(id);
    }

    public List<Building> getBuildingsByStatus(BuildingStatus status) {
        return buildingRepository.findByStatus(status);
    }

    public List<Building> getBuildingsByCustomer(Long customerId) {
        return buildingRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Building createBuilding(Building building) {
        building.setId(null);
        building.setCreatedAt(LocalDateTime.now());
        building.setUpdatedAt(LocalDateTime.now());
        return buildingRepository.save(building);
    }

    @Transactional
    public Building updateBuilding(Long id, Building building) {
        Building existingBuilding = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + id));

        // Update only the editable fields, preserve floors relationship
        existingBuilding.setName(building.getName());
        existingBuilding.setAddress(building.getAddress());
        existingBuilding.setDescription(building.getDescription());
        existingBuilding.setNumberOfFloors(building.getNumberOfFloors());
        existingBuilding.setStatus(building.getStatus());
        existingBuilding.setCustomer(building.getCustomer());
        existingBuilding.setUpdatedAt(LocalDateTime.now());

        return buildingRepository.save(existingBuilding);
    }

    @Transactional
    public void deleteBuilding(Long id) {
        if (!buildingRepository.existsById(id)) {
            throw new RuntimeException("Building not found with id: " + id);
        }
        buildingRepository.deleteById(id);
    }
}
