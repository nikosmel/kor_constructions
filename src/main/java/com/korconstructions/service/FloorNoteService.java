package com.korconstructions.service;

import com.korconstructions.model.Floor;
import com.korconstructions.model.FloorNote;
import com.korconstructions.repository.FloorNoteRepository;
import com.korconstructions.repository.FloorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FloorNoteService {

    @Autowired
    private FloorNoteRepository noteRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Transactional(readOnly = true)
    public List<FloorNote> getNotesByFloor(Long floorId) {
        return noteRepository.findByFloorId(floorId);
    }

    @Transactional
    public FloorNote createNote(Long floorId, FloorNote note) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Floor not found"));
        note.setId(null);
        note.setFloor(floor);
        return noteRepository.save(note);
    }

    @Transactional
    public FloorNote updateNote(Long floorId, Long noteId, FloorNote note) {
        FloorNote existing = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        if (!existing.getFloor().getId().equals(floorId)) {
            throw new RuntimeException("Note does not belong to this floor");
        }
        existing.setTitle(note.getTitle());
        existing.setDescription(note.getDescription());
        return noteRepository.save(existing);
    }

    @Transactional
    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }
}
