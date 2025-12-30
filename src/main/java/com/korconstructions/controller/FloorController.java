package com.korconstructions.controller;

import com.korconstructions.model.Floor;
import com.korconstructions.service.FloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/floors")
public class FloorController {

    private final FloorService floorService;

    @Value("${app.upload.floor-dir:./uploads/floors}")
    private String uploadDir;

    @Autowired
    public FloorController(FloorService floorService) {
        this.floorService = floorService;
    }

    @GetMapping
    public ResponseEntity<List<Floor>> getAllFloors() {
        return ResponseEntity.ok(floorService.getAllFloors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Floor> getFloorById(@PathVariable Long id) {
        return floorService.getFloorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<Floor>> getFloorsByBuilding(@PathVariable Long buildingId) {
        return ResponseEntity.ok(floorService.getFloorsByBuilding(buildingId));
    }

    @PostMapping
    public ResponseEntity<Floor> createFloor(@RequestBody Floor floor) {
        Floor created = floorService.createFloor(floor);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Floor> updateFloor(@PathVariable Long id, @RequestBody Floor floor) {
        try {
            Floor updated = floorService.updateFloor(id, floor);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFloor(@PathVariable Long id) {
        try {
            floorService.deleteFloor(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<Map<String, Object>> uploadFloorImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // Save file
            file.transferTo(filePath.toFile());

            // Update floor with image path
            Floor floor = floorService.getFloorById(id)
                    .orElseThrow(() -> new RuntimeException("Floor not found"));
            // Use web-accessible path (remove ./ prefix)
            String webPath = "/uploads/floors/" + filename;
            floor.setImagePath(webPath);
            floorService.updateFloor(id, floor);

            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("imagePath", floor.getImagePath());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
