package com.korconstructions.controller;

import com.korconstructions.model.FloorNote;
import com.korconstructions.service.FloorNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/floors/{floorId}/notes")
public class FloorNoteController {

    @Autowired
    private FloorNoteService noteService;

    @GetMapping
    public ResponseEntity<List<FloorNote>> getNotes(@PathVariable Long floorId) {
        return ResponseEntity.ok(noteService.getNotesByFloor(floorId));
    }

    @PostMapping
    public ResponseEntity<FloorNote> createNote(
            @PathVariable Long floorId,
            @RequestBody FloorNote note) {
        FloorNote created = noteService.createNote(floorId, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<FloorNote> updateNote(
            @PathVariable Long floorId,
            @PathVariable Long noteId,
            @RequestBody FloorNote note) {
        try {
            FloorNote updated = noteService.updateNote(floorId, noteId, note);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        try {
            noteService.deleteNote(noteId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
