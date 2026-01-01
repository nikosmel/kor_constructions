package com.korconstructions.repository;

import com.korconstructions.model.Document;
import com.korconstructions.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByType(DocumentType type);
    List<Document> findByBuildingId(Long buildingId);
    List<Document> findByTitleContainingIgnoreCase(String title);
}
