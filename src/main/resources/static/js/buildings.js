// Buildings and Floors management
const BUILDINGS_API = '/api/buildings';
const FLOORS_API = '/api/floors';
const CUSTOMERS_API = '/api/customers';

let editingBuildingId = null;
let editingFloorId = null;
let currentFloorImageFile = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    initializeBuildingsTabs();
    setupBuildingForm();
    setupFloorForm();
    setupAddBuildingButton();
    setupAddFloorButton();
    loadCustomersForBuildingSelect();
    loadBuildingsForFloorSelect();
    loadBuildings(); // Auto-load buildings on page load
});

function initializeBuildingsTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabName = button.getAttribute('data-tab');

            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            button.classList.add('active');
            document.getElementById(`${tabName}-tab`).classList.add('active');

            if (tabName === 'buildings') {
                loadBuildings();
                loadCustomersForBuildingSelect();
            } else if (tabName === 'floors') {
                loadFloors();
                loadBuildingsForFloorSelect();
            }
        });
    });
}

// ============ BUILDINGS SECTION ============

// Modal Functions
function openBuildingModal() {
    resetBuildingForm();
    document.getElementById('building-modal-title').textContent = 'Νέο Κτίριο';
    document.getElementById('building-modal').style.display = 'flex';
}

function closeBuildingModal() {
    document.getElementById('building-modal').style.display = 'none';
    resetBuildingForm();
}

function setupAddBuildingButton() {
    const addBtn = document.getElementById('add-building-btn');
    if (addBtn) {
        addBtn.addEventListener('click', openBuildingModal);
    }
}

function setupBuildingForm() {
    const form = document.getElementById('building-form');

    form.addEventListener('submit', handleBuildingFormSubmit);
}

async function loadBuildings() {
    try {
        const response = await fetch(BUILDINGS_API);
        if (!response.ok) throw new Error('Failed to fetch buildings');

        const buildings = await response.json();
        displayBuildings(buildings);
    } catch (error) {
        console.error('Error loading buildings:', error);
        showError('Αποτυχία φόρτωσης κτιρίων');
    }
}

function displayBuildings(buildings) {
    const container = document.getElementById('buildings-container');

    if (buildings.length === 0) {
        container.innerHTML = '<div class="empty-state"><h3>Δεν υπάρχουν κτίρια</h3><p>Προσθέστε το πρώτο κτίριο χρησιμοποιώντας τη φόρμα</p></div>';
        return;
    }

    container.innerHTML = buildings.map(building => {
        const statusBadge = getStatusBadge(building.status);
        const customerName = building.customer ? building.customer.name : 'Χωρίς πελάτη';

        return `
            <div class="customer-card">
                <div class="card-header">
                    <div class="card-title">${escapeHtml(building.name)}</div>
                    <div class="card-badge" style="${statusBadge.style}">${statusBadge.text}</div>
                </div>
                <div class="card-body">
                    <div class="card-info">
                        <div class="info-item"><strong>Διεύθυνση:</strong> ${escapeHtml(building.address)}</div>
                        <div class="info-item"><strong>Πελάτης:</strong> ${escapeHtml(customerName)}</div>
                        ${building.numberOfFloors ? `<div class="info-item"><strong>Αριθμός Ορόφων:</strong> ${building.numberOfFloors}</div>` : ''}
                        ${building.description ? `<div class="info-item"><strong>Περιγραφή:</strong> ${escapeHtml(building.description)}</div>` : ''}
                        <div class="info-item"><strong>Διαμερίσματα:</strong> ${building.floors ? building.floors.length : 0}</div>
                    </div>
                </div>
                <div class="card-actions">
                    <button class="btn btn-view" onclick="viewBuildingFloors(${building.id})">Διαμερίσματα</button>
                    <button class="btn btn-edit" onclick="editBuilding(${building.id})">Επεξεργασία</button>
                    <button class="btn btn-delete" onclick="deleteBuilding(${building.id})">Διαγραφή</button>
                </div>
            </div>
        `;
    }).join('');
}

function getStatusBadge(status) {
    const badges = {
        'PLANNING': { text: 'Σχεδιασμός', style: 'background: #fff3cd; color: #856404;' },
        'IN_PROGRESS': { text: 'Σε Εξέλιξη', style: 'background: #d1ecf1; color: #0c5460;' },
        'COMPLETED': { text: 'Ολοκληρωμένο', style: 'background: #d4edda; color: #155724;' }
    };
    return badges[status] || { text: status, style: '' };
}

async function handleBuildingFormSubmit(e) {
    e.preventDefault();

    const customerId = document.getElementById('building-customer').value;
    const numberOfFloors = document.getElementById('building-number-of-floors').value;
    const building = {
        name: document.getElementById('building-name').value,
        address: document.getElementById('building-address').value,
        description: document.getElementById('building-description').value,
        numberOfFloors: numberOfFloors ? parseInt(numberOfFloors) : null,
        status: document.getElementById('building-status').value,
        customer: customerId ? { id: parseInt(customerId) } : null
    };

    try {
        if (editingBuildingId) {
            await updateBuilding(editingBuildingId, building);
        } else {
            await createBuilding(building);
        }
        closeBuildingModal();
        loadBuildings();
        loadBuildingsForFloorSelect();
    } catch (error) {
        console.error('Error saving building:', error);
        showError('Αποτυχία αποθήκευσης κτιρίου');
    }
}

async function createBuilding(building) {
    const response = await fetch(BUILDINGS_API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(building)
    });
    if (!response.ok) throw new Error('Failed to create building');
    return response.json();
}

async function updateBuilding(id, building) {
    const response = await fetch(`${BUILDINGS_API}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(building)
    });
    if (!response.ok) throw new Error('Failed to update building');
    return response.json();
}

async function editBuilding(id) {
    try {
        const response = await fetch(`${BUILDINGS_API}/${id}`);
        if (!response.ok) throw new Error('Failed to fetch building');

        const building = await response.json();
        document.getElementById('building-name').value = building.name;
        document.getElementById('building-address').value = building.address;
        document.getElementById('building-number-of-floors').value = building.numberOfFloors || '';
        document.getElementById('building-description').value = building.description || '';
        document.getElementById('building-status').value = building.status;
        document.getElementById('building-customer').value = building.customer ? building.customer.id : '';

        editingBuildingId = id;
        document.getElementById('building-submit-btn').textContent = 'Ενημέρωση';
        document.getElementById('building-modal-title').textContent = 'Επεξεργασία Κτιρίου';
        document.getElementById('building-modal').style.display = 'flex';
    } catch (error) {
        console.error('Error loading building:', error);
        showError('Αποτυχία φόρτωσης κτιρίου');
    }
}

async function deleteBuilding(id) {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτό το κτίριο; Θα διαγραφούν και όλα τα διαμερίσματα.')) return;

    try {
        const response = await fetch(`${BUILDINGS_API}/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete building');
        loadBuildings();
        loadBuildingsForFloorSelect();
    } catch (error) {
        console.error('Error deleting building:', error);
        showError('Αποτυχία διαγραφής κτιρίου');
    }
}

function resetBuildingForm() {
    document.getElementById('building-form').reset();
    editingBuildingId = null;
    document.getElementById('building-submit-btn').textContent = 'Αποθήκευση';
}

async function loadCustomersForBuildingSelect() {
    try {
        const response = await fetch(CUSTOMERS_API);
        if (!response.ok) throw new Error('Failed to fetch customers');

        const customers = await response.json();
        const select = document.getElementById('building-customer');

        select.innerHTML = '<option value="">Επιλέξτε πελάτη</option>' +
            customers.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join('');
    } catch (error) {
        console.error('Error loading customers:', error);
    }
}

function viewBuildingFloors(buildingId) {
    document.querySelector('[data-tab="floors"]').click();
    setTimeout(() => {
        document.getElementById('filter-building').value = buildingId;
        filterFloorsByBuilding();
    }, 100);
}

// ============ FLOORS SECTION ============

// Modal Functions
function openFloorModal() {
    resetFloorForm();
    document.getElementById('floor-modal-title').textContent = 'Νέο Διαμέρισμα';
    document.getElementById('floor-modal').style.display = 'flex';
}

function closeFloorModal() {
    document.getElementById('floor-modal').style.display = 'none';
    resetFloorForm();

    // Hide and reset notes section
    const notesSection = document.getElementById('floor-notes-section');
    if (notesSection) {
        notesSection.style.display = 'none';
    }
    if (typeof resetNoteForm === 'function') {
        resetNoteForm();
    }
}

function setupAddFloorButton() {
    const addBtn = document.getElementById('add-floor-btn');
    if (addBtn) {
        addBtn.addEventListener('click', openFloorModal);
    }
}

function setupFloorForm() {
    const form = document.getElementById('floor-form');
    const imageInput = document.getElementById('floor-image');
    const filterSelect = document.getElementById('filter-building');

    form.addEventListener('submit', handleFloorFormSubmit);
    imageInput.addEventListener('change', handleFloorImagePreview);
    filterSelect.addEventListener('change', filterFloorsByBuilding);
}

function handleFloorImagePreview(e) {
    const file = e.target.files[0];
    if (file) {
        currentFloorImageFile = file;
        const reader = new FileReader();
        reader.onload = (e) => {
            document.getElementById('floor-preview-img').src = e.target.result;
            document.getElementById('floor-image-preview').style.display = 'block';
        };
        reader.readAsDataURL(file);
    }
}

async function loadFloors() {
    try {
        const response = await fetch(FLOORS_API);
        if (!response.ok) throw new Error('Failed to fetch floors');

        const floors = await response.json();
        displayFloors(floors);
    } catch (error) {
        console.error('Error loading floors:', error);
        showError('Αποτυχία φόρτωσης ορόφων');
    }
}

function displayFloors(floors) {
    const container = document.getElementById('floors-container');

    if (floors.length === 0) {
        container.innerHTML = '<div class="empty-state"><h3>Δεν υπάρχουν διαμερίσματα</h3><p>Προσθέστε το πρώτο διαμέρισμα χρησιμοποιώντας τη φόρμα</p></div>';
        return;
    }

    container.innerHTML = floors.map(floor => {
        const buildingName = floor.building ? floor.building.name : 'N/A';

        return `
            <div class="customer-card">
                <div class="card-header">
                    <div class="card-title">Διαμέρισμα ${escapeHtml(buildingName)} - Όροφος ${escapeHtml(floor.floorNumber)}</div>
                    ${floor.price ? `<div class="card-badge">${formatCurrency(floor.price)}</div>` : ''}
                </div>
                <div class="card-body">
                    <div class="card-info">
                        ${floor.description ? `<div class="info-item"><strong>Περιγραφή:</strong> ${escapeHtml(floor.description)}</div>` : ''}
                        ${floor.squareMeters ? `<div class="info-item"><strong>Τετραγωνικά:</strong> ${floor.squareMeters} m²</div>` : ''}
                        ${floor.details ? `<div class="info-item"><strong>Λεπτομέρειες:</strong> ${escapeHtml(floor.details)}</div>` : ''}
                    </div>
                    ${floor.imagePath ? `<div style="margin-top: 15px;"><img src="${floor.imagePath}" style="max-width: 100%; max-height: 200px; border-radius: 5px; cursor: pointer;" onclick="openImageViewer('${floor.imagePath}')" title="Κλικ για μεγέθυνση"></div>` : ''}
                </div>
                <div class="card-actions">
                    <button class="btn btn-edit" onclick="editFloor(${floor.id})">Επεξεργασία</button>
                    <button class="btn btn-delete" onclick="deleteFloor(${floor.id})">Διαγραφή</button>
                </div>
            </div>
        `;
    }).join('');
}

async function handleFloorFormSubmit(e) {
    e.preventDefault();

    const buildingId = document.getElementById('floor-building').value;
    const floor = {
        floorNumber: document.getElementById('floor-number').value,
        description: document.getElementById('floor-description').value,
        squareMeters: document.getElementById('floor-square-meters').value || null,
        price: document.getElementById('floor-price').value || null,
        details: document.getElementById('floor-details').value,
        building: { id: parseInt(buildingId) }
    };

    try {
        let savedFloor;
        if (editingFloorId) {
            savedFloor = await updateFloor(editingFloorId, floor);
        } else {
            savedFloor = await createFloor(floor);
        }

        if (currentFloorImageFile) {
            await uploadFloorImage(savedFloor.id, currentFloorImageFile);
        }

        closeFloorModal();
        loadFloors();
    } catch (error) {
        console.error('Error saving floor:', error);
        showError('Αποτυχία αποθήκευσης ορόφου');
    }
}

async function createFloor(floor) {
    const response = await fetch(FLOORS_API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(floor)
    });
    if (!response.ok) throw new Error('Failed to create floor');
    return response.json();
}

async function updateFloor(id, floor) {
    const response = await fetch(`${FLOORS_API}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(floor)
    });
    if (!response.ok) throw new Error('Failed to update floor');
    return response.json();
}

async function uploadFloorImage(floorId, file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${FLOORS_API}/${floorId}/upload-image`, {
        method: 'POST',
        body: formData
    });

    if (!response.ok) throw new Error('Failed to upload image');
    return response.json();
}

async function editFloor(id) {
    try {
        // Ensure building dropdown is populated first
        await loadBuildingsForFloorSelect();

        const response = await fetch(`${FLOORS_API}/${id}`);
        if (!response.ok) throw new Error('Failed to fetch floor');

        const floor = await response.json();

        // Use setTimeout to ensure DOM is ready
        setTimeout(() => {
            document.getElementById('floor-building').value = floor.building ? floor.building.id : '';
            document.getElementById('floor-number').value = floor.floorNumber;
            document.getElementById('floor-description').value = floor.description || '';
            document.getElementById('floor-square-meters').value = floor.squareMeters || '';
            document.getElementById('floor-price').value = floor.price || '';
            document.getElementById('floor-details').value = floor.details || '';

            if (floor.imagePath) {
                document.getElementById('floor-preview-img').src = floor.imagePath;
                document.getElementById('floor-image-preview').style.display = 'block';
            }

            editingFloorId = id;
            document.getElementById('floor-submit-btn').textContent = 'Ενημέρωση';
            document.getElementById('floor-modal-title').textContent = 'Επεξεργασία Διαμερίσματος';
            document.getElementById('floor-modal').style.display = 'flex';

            // Load notes for this floor
            openFloorNoteEditor(id);
        }, 100);
    } catch (error) {
        console.error('Error loading floor:', error);
        showError('Αποτυχία φόρτωσης ορόφου');
    }
}

async function deleteFloor(id) {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτό το διαμέρισμα;')) return;

    try {
        const response = await fetch(`${FLOORS_API}/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete floor');
        loadFloors();
    } catch (error) {
        console.error('Error deleting floor:', error);
        showError('Αποτυχία διαγραφής ορόφου');
    }
}

function resetFloorForm() {
    document.getElementById('floor-form').reset();
    document.getElementById('floor-image-preview').style.display = 'none';
    currentFloorImageFile = null;
    editingFloorId = null;
    document.getElementById('floor-submit-btn').textContent = 'Αποθήκευση';
}

async function loadBuildingsForFloorSelect() {
    try {
        const response = await fetch(BUILDINGS_API);
        if (!response.ok) throw new Error('Failed to fetch buildings');

        const buildings = await response.json();

        const floorSelect = document.getElementById('floor-building');
        floorSelect.innerHTML = '<option value="">Επιλέξτε κτίριο</option>' +
            buildings.map(b => `<option value="${b.id}">${escapeHtml(b.name)}</option>`).join('');

        const filterSelect = document.getElementById('filter-building');
        filterSelect.innerHTML = '<option value="">Όλα τα κτίρια</option>' +
            buildings.map(b => `<option value="${b.id}">${escapeHtml(b.name)}</option>`).join('');
    } catch (error) {
        console.error('Error loading buildings for select:', error);
    }
}

async function filterFloorsByBuilding() {
    const buildingId = document.getElementById('filter-building').value;

    try {
        let url = FLOORS_API;
        if (buildingId) {
            url = `${FLOORS_API}/building/${buildingId}`;
        }

        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to fetch floors');

        const floors = await response.json();
        displayFloors(floors);
    } catch (error) {
        console.error('Error filtering floors:', error);
        showError('Αποτυχία φιλτραρίσματος ορόφων');
    }
}

// ============ IMAGE VIEWER ============

function openImageViewer(imagePath) {
    document.getElementById('viewer-image').src = imagePath;
    document.getElementById('image-viewer-modal').style.display = 'flex';
}

function closeImageViewer() {
    document.getElementById('image-viewer-modal').style.display = 'none';
    document.getElementById('viewer-image').src = '';
}
