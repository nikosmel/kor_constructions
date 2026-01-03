// State
let currentFloorId = null;
let editingNoteId = null;

// Initialize when floor modal opens
async function openFloorNoteEditor(floorId) {
    currentFloorId = floorId;
    editingNoteId = null;

    // Show notes section
    const notesSection = document.getElementById('floor-notes-section');
    if (notesSection) {
        notesSection.style.display = 'block';
    }

    await loadFloorNotes(floorId);
    setupNoteFormListeners();
}

// Setup form listeners
function setupNoteFormListeners() {
    const descField = document.getElementById('note-description');
    if (descField) {
        descField.addEventListener('input', updateCharCount);
        updateCharCount(); // Initial count
    }
}

// Character counter
function updateCharCount() {
    const desc = document.getElementById('note-description').value;
    document.getElementById('char-count').textContent = `${desc.length} / 2000`;
}

// Load and display notes
async function loadFloorNotes(floorId) {
    const notesList = document.getElementById('notes-list');
    notesList.innerHTML = '<p class="text-center text-muted">Φόρτωση...</p>';

    try {
        const response = await fetch(`/api/floors/${floorId}/notes`);
        const notes = await response.json();
        displayFloorNotes(notes);
    } catch (error) {
        console.error('Error loading notes:', error);
        notesList.innerHTML = '<p class="text-danger">Σφάλμα φόρτωσης σημειώσεων</p>';
    }
}

// Display notes list
function displayFloorNotes(notes) {
    const notesList = document.getElementById('notes-list');

    if (notes.length === 0) {
        notesList.innerHTML = '<p class="text-muted text-center">Δεν υπάρχουν σημειώσεις</p>';
        return;
    }

    notesList.innerHTML = notes.map(note => `
        <div class="note-card" style="border: 1px solid #ddd; padding: 10px; margin-bottom: 10px; border-radius: 5px; background: white;">
            <div style="display: flex; justify-content: space-between; align-items: start;">
                <div style="flex: 1;">
                    <h5 style="margin: 0 0 5px 0;">${escapeHtml(note.title)}</h5>
                    <p style="margin: 0 0 5px 0; color: #666; white-space: pre-wrap;">${escapeHtml(note.description || '')}</p>
                    <small class="text-muted">Δημιουργήθηκε: ${formatDateTime(note.createdAt)}</small>
                </div>
                <div style="display: flex; gap: 5px; flex-shrink: 0; margin-left: 10px;">
                    <button class="btn btn-sm btn-edit" onclick="editFloorNote(${note.id}, \`${escapeHtml(note.title)}\`, \`${escapeHtml(note.description || '')}\`)">Επεξ.</button>
                    <button class="btn btn-sm btn-delete" onclick="deleteFloorNote(${note.id})">Διαγρ.</button>
                </div>
            </div>
        </div>
    `).join('');
}

// Create or update note
async function handleNoteFormSubmit(event) {
    event.preventDefault();

    const title = document.getElementById('note-title').value.trim();
    const description = document.getElementById('note-description').value.trim();

    if (!title) {
        alert('Ο τίτλος είναι υποχρεωτικός');
        return;
    }

    const noteData = { title, description };

    try {
        if (editingNoteId) {
            // Update
            await fetch(`/api/floors/${currentFloorId}/notes/${editingNoteId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(noteData)
            });
        } else {
            // Create
            await fetch(`/api/floors/${currentFloorId}/notes`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(noteData)
            });
        }

        resetNoteForm();
        await loadFloorNotes(currentFloorId);
    } catch (error) {
        console.error('Error saving note:', error);
        alert('Σφάλμα κατά την αποθήκευση');
    }
}

// Edit note
function editFloorNote(noteId, title, description) {
    editingNoteId = noteId;
    document.getElementById('note-id').value = noteId;
    document.getElementById('note-title').value = title;
    document.getElementById('note-description').value = description;
    document.getElementById('note-form-title').textContent = 'Επεξεργασία Σημείωσης';
    document.getElementById('note-submit-btn').textContent = 'Ενημέρωση';
    document.getElementById('note-cancel-btn').style.display = 'inline-block';
    updateCharCount();
}

// Cancel edit
function cancelNoteEdit() {
    resetNoteForm();
}

// Reset form
function resetNoteForm() {
    editingNoteId = null;
    document.getElementById('note-id').value = '';
    document.getElementById('note-title').value = '';
    document.getElementById('note-description').value = '';
    document.getElementById('note-form-title').textContent = 'Νέα Σημείωση';
    document.getElementById('note-submit-btn').textContent = 'Αποθήκευση';
    document.getElementById('note-cancel-btn').style.display = 'none';
    updateCharCount();
}

// Delete note
async function deleteFloorNote(noteId) {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτή τη σημείωση;')) {
        return;
    }

    try {
        await fetch(`/api/floors/${currentFloorId}/notes/${noteId}`, {
            method: 'DELETE'
        });
        await loadFloorNotes(currentFloorId);
    } catch (error) {
        console.error('Error deleting note:', error);
        alert('Σφάλμα κατά τη διαγραφή');
    }
}

// Utility functions (use existing ones from app.js if available)
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML.replace(/'/g, '&#39;').replace(/`/g, '&#96;');
}

function formatDateTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('el-GR');
}
