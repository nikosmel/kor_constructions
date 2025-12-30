package com.korconstructions.service;

import com.korconstructions.model.Floor;
import com.korconstructions.repository.FloorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FloorService {

    private final FloorRepository floorRepository;

    @Autowired
    public FloorService(FloorRepository floorRepository) {
        this.floorRepository = floorRepository;
    }

    public List<Floor> getAllFloors() {
        return floorRepository.findAll();
    }

    public Optional<Floor> getFloorById(Long id) {
        return floorRepository.findById(id);
    }

    public List<Floor> getFloorsByBuilding(Long buildingId) {
        return floorRepository.findByBuildingId(buildingId);
    }

    @Transactional
    public Floor createFloor(Floor floor) {
        floor.setId(null);
        floor.setCreatedAt(LocalDateTime.now());
        floor.setUpdatedAt(LocalDateTime.now());
        return floorRepository.save(floor);
    }

    @Transactional
    public Floor updateFloor(Long id, Floor floor) {
        Floor existingFloor = floorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Floor not found with id: " + id));

        // Update only the editable fields, preserve imagePath
        existingFloor.setFloorNumber(floor.getFloorNumber());
        existingFloor.setDescription(floor.getDescription());
        existingFloor.setSquareMeters(floor.getSquareMeters());
        existingFloor.setPrice(floor.getPrice());
        existingFloor.setDetails(floor.getDetails());
        existingFloor.setBuilding(floor.getBuilding());
        existingFloor.setUpdatedAt(LocalDateTime.now());
        // Note: imagePath is NOT updated here - it's only updated via uploadFloorImage endpoint

        return floorRepository.save(existingFloor);
    }

    @Transactional
    public void deleteFloor(Long id) {
        if (!floorRepository.existsById(id)) {
            throw new RuntimeException("Floor not found with id: " + id);
        }
        floorRepository.deleteById(id);
    }
}
