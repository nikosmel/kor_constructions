// Tab switching
document.querySelectorAll('.tab-button').forEach(button => {
    button.addEventListener('click', () => {
        const tabName = button.dataset.tab;

        // Remove active class from all tabs and buttons
        document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

        // Add active class to clicked button and corresponding tab
        button.classList.add('active');
        document.getElementById(`${tabName}-tab`).classList.add('active');
    });
});

// Document type labels in Greek
const documentTypeLabels = {
    'CONTRACT': 'Σύμβαση',
    'INVOICE': 'Τιμολόγιο',
    'BLUEPRINT': 'Σχέδιο',
    'PERMIT': 'Άδεια',
    'TECHNICAL_SPEC': 'Τεχνική Προδιαγραφή',
    'INSPECTION_REPORT': 'Έκθεση Επιθεώρησης',
    'OTHER': 'Άλλο'
};

let allDocuments = [];
let allBuildings = [];

// Load buildings for dropdowns
async function loadBuildings() {
    try {
        const response = await fetch('/api/buildings');
        allBuildings = await response.json();

        // Populate building dropdowns
        const buildingSelects = [
            document.getElementById('document-building'),
            document.getElementById('filter-building-doc'),
            document.getElementById('query-building')
        ];

        buildingSelects.forEach(select => {
            // Clear existing options except first
            while (select.options.length > 1) {
                select.remove(1);
            }

            allBuildings.forEach(building => {
                const option = document.createElement('option');
                option.value = building.id;
                option.textContent = building.name;
                select.appendChild(option);
            });
        });
    } catch (error) {
        console.error('Failed to load buildings:', error);
    }
}

// Load documents
async function loadDocuments() {
    try {
        const response = await fetch('/api/documents');
        allDocuments = await response.json();
        displayDocuments(allDocuments);
    } catch (error) {
        console.error('Failed to load documents:', error);
        document.getElementById('documents-container').innerHTML = '<p class="error">Σφάλμα φόρτωσης εγγράφων</p>';
    }
}

// Display documents
function displayDocuments(documents) {
    const container = document.getElementById('documents-container');

    if (documents.length === 0) {
        container.innerHTML = '<p class="empty-state">Δεν υπάρχουν έγγραφα</p>';
        return;
    }

    container.innerHTML = documents.map(doc => `
        <div class="card document-card">
            <div class="card-header">
                <div>
                    <h3>${doc.title}</h3>
                    <span class="badge badge-${doc.type.toLowerCase()}">${documentTypeLabels[doc.type]}</span>
                </div>
                <div class="card-actions">
                    ${doc.mimeType === 'application/pdf' ? `
                        <button class="btn-icon" onclick="viewDocument(${doc.id})" title="Άνοιγμα">
                            👁️
                        </button>
                    ` : ''}
                    <button class="btn-icon" onclick="downloadDocument(${doc.id})" title="Λήψη">
                        📥
                    </button>
                    <button class="btn-icon" onclick="deleteDocument(${doc.id})" title="Διαγραφή">
                        🗑️
                    </button>
                </div>
            </div>
            <div class="card-body">
                ${doc.description ? `<p class="description">${doc.description}</p>` : ''}
                <div class="document-meta">
                    <div class="meta-item">
                        <strong>Αρχείο:</strong> ${doc.fileName}
                    </div>
                    <div class="meta-item">
                        <strong>Μέγεθος:</strong> ${formatFileSize(doc.fileSize)}
                    </div>
                    ${doc.building ? `
                        <div class="meta-item">
                            <strong>Κτίριο:</strong> ${doc.building.name}
                        </div>
                    ` : ''}
                    <div class="meta-item">
                        <strong>Ανέβηκε:</strong> ${formatDateTime(doc.uploadedAt)}
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// Filter documents
function filterDocuments() {
    const typeFilter = document.getElementById('filter-type').value;
    const buildingFilter = document.getElementById('filter-building-doc').value;

    let filtered = allDocuments;

    if (typeFilter) {
        filtered = filtered.filter(doc => doc.type === typeFilter);
    }

    if (buildingFilter) {
        filtered = filtered.filter(doc =>
            doc.building && doc.building.id.toString() === buildingFilter
        );
    }

    displayDocuments(filtered);
}

// Modal controls
function openDocumentModal() {
    document.getElementById('document-form').reset();
    document.getElementById('document-modal').style.display = 'flex';
}

function closeDocumentModal() {
    document.getElementById('document-modal').style.display = 'none';
}

// Upload document
document.getElementById('document-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const fileInput = document.getElementById('document-file');
    const file = fileInput.files[0];

    if (!file) {
        alert('Παρακαλώ επιλέξτε αρχείο');
        return;
    }

    // Create FormData for file upload
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', document.getElementById('document-title').value);
    formData.append('description', document.getElementById('document-description').value || '');
    formData.append('type', document.getElementById('document-type').value);

    const buildingId = document.getElementById('document-building').value;
    if (buildingId) {
        formData.append('buildingId', buildingId);
    }

    try {
        // Show loading state
        const submitBtn = e.target.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.textContent = '⏳ Ανέβασμα και επεξεργασία με AI...';
        submitBtn.disabled = true;

        const response = await fetch('/api/documents', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            closeDocumentModal();
            await loadDocuments();
            alert('Το έγγραφο ανέβηκε και επεξεργάστηκε επιτυχώς!');
        } else {
            alert('Σφάλμα κατά το ανέβασμα του εγγράφου');
        }

        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    } catch (error) {
        console.error('Failed to upload document:', error);
        alert('Σφάλμα κατά το ανέβασμα του εγγράφου');
    }
});

// View document (opens in new tab for PDFs)
function viewDocument(id) {
    window.open(`/api/documents/${id}/download?inline=true`, '_blank');
}

// Download document
function downloadDocument(id) {
    window.location.href = `/api/documents/${id}/download`;
}

// Delete document
async function deleteDocument(id) {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτό το έγγραφο;')) {
        return;
    }

    try {
        const response = await fetch(`/api/documents/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            await loadDocuments();
        } else {
            alert('Σφάλμα κατά τη διαγραφή του εγγράφου');
        }
    } catch (error) {
        console.error('Failed to delete document:', error);
        alert('Σφάλμα κατά τη διαγραφή του εγγράφου');
    }
}

// AI Query functionality
document.getElementById('ask-question-btn').addEventListener('click', async () => {
    const question = document.getElementById('question-input').value.trim();

    if (!question) {
        alert('Παρακαλώ πληκτρολογήστε μια ερώτηση');
        return;
    }

    const buildingId = document.getElementById('query-building').value || null;

    const btn = document.getElementById('ask-question-btn');
    const originalText = btn.textContent;
    btn.textContent = '🤔 Το AI σκέφτεται...';
    btn.disabled = true;

    try {
        const response = await fetch('/api/documents/ask', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                question: question,
                buildingId: buildingId
            })
        });

        if (response.ok) {
            const data = await response.json();
            displayAnswer(data.answer);
        } else {
            alert('Σφάλμα κατά την επεξεργασία της ερώτησης');
        }
    } catch (error) {
        console.error('Failed to ask question:', error);
        alert('Σφάλμα κατά την επεξεργασία της ερώτησης');
    } finally {
        btn.textContent = originalText;
        btn.disabled = false;
    }
});

function displayAnswer(answer) {
    const answerDiv = document.getElementById('ai-answer');
    const answerContent = answerDiv.querySelector('.answer-content');

    answerContent.textContent = answer;
    answerDiv.style.display = 'block';

    // Scroll to answer
    answerDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// Utility functions
function formatFileSize(bytes) {
    if (!bytes) return 'N/A';
    const kb = bytes / 1024;
    if (kb < 1024) {
        return kb.toFixed(1) + ' KB';
    }
    const mb = kb / 1024;
    return mb.toFixed(1) + ' MB';
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '';
    const date = new Date(dateTimeString);
    return date.toLocaleString('el-GR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Event listeners
document.getElementById('add-document-btn').addEventListener('click', openDocumentModal);
document.getElementById('filter-type').addEventListener('change', filterDocuments);
document.getElementById('filter-building-doc').addEventListener('change', filterDocuments);

// Close modal when clicking outside
document.getElementById('document-modal').addEventListener('click', (e) => {
    if (e.target.id === 'document-modal') {
        closeDocumentModal();
    }
});

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    loadBuildings();
    loadDocuments();
});
