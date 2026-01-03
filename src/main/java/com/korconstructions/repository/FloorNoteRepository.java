package com.korconstructions.repository;

import com.korconstructions.model.FloorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloorNoteRepository extends JpaRepository<FloorNote, Long> {
    List<FloorNote> findByFloorId(Long floorId);
}
