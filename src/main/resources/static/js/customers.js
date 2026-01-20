// Customers management
// API constants are defined in app.js

let editingCustomerId = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupCustomerForm();
    setupAddCustomerButton();
});

// Modal Functions
function openCustomerModal() {
    resetCustomerForm();
    document.getElementById('customer-modal-title').textContent = 'Νέος Πελάτης';
    document.getElementById('customer-modal').style.display = 'flex';
}

function closeCustomerModal() {
    document.getElementById('customer-modal').style.display = 'none';
    resetCustomerForm();
}

function setupAddCustomerButton() {
    const addBtn = document.getElementById('add-customer-btn');
    if (addBtn) {
        addBtn.addEventListener('click', openCustomerModal);
    }
}

function setupCustomerForm() {
    const form = document.getElementById('customer-form');
    form.addEventListener('submit', handleCustomerFormSubmit);
}

async function loadCustomers() {
    try {
        const response = await fetch(CUSTOMERS_API);
        if (!response.ok) throw new Error('Failed to fetch customers');

        const customers = await response.json();
        displayCustomers(customers);
    } catch (error) {
        console.error('Error loading customers:', error);
        showError('Αποτυχία φόρτωσης πελατών');
    }
}

function displayCustomers(customers) {
    const container = document.getElementById('customers-container');

    if (customers.length === 0) {
        container.innerHTML = '<div class="empty-state"><h3>Δεν υπάρχουν πελάτες</h3><p>Προσθέστε τον πρώτο πελάτη χρησιμοποιώντας τη φόρμα</p></div>';
        return;
    }

    container.innerHTML = customers.map(customer => `
        <div class="customer-card">
            <div class="card-header">
                <div class="card-title">${escapeHtml(customer.name)}</div>
            </div>
            <div class="card-body">
                <div class="card-info">
                    ${customer.phone ? `<div class="info-item"><strong>Τηλ:</strong> ${escapeHtml(customer.phone)}</div>` : ''}
                    ${customer.email ? `<div class="info-item"><strong>Email:</strong> ${escapeHtml(customer.email)}</div>` : ''}
                    ${customer.afm ? `<div class="info-item"><strong>ΑΦΜ:</strong> ${escapeHtml(customer.afm)}</div>` : ''}
                    ${customer.address ? `<div class="info-item"><strong>Διεύθυνση:</strong> ${escapeHtml(customer.address)}</div>` : ''}
                </div>
                ${customer.notes ? `<div class="info-item"><strong>Σημειώσεις:</strong> ${escapeHtml(customer.notes)}</div>` : ''}
            </div>
            <div class="card-actions">
                <button class="btn btn-view" onclick="viewCustomerReceipts(${customer.id}, '${escapeHtml(customer.name)}')">Αποδείξεις</button>
                <button class="btn btn-edit" onclick="editCustomer(${customer.id})">Επεξεργασία</button>
                <button class="btn btn-delete" onclick="deleteCustomer(${customer.id})">Διαγραφή</button>
            </div>
        </div>
    `).join('');
}

async function handleCustomerFormSubmit(e) {
    e.preventDefault();

    const customer = {
        name: document.getElementById('customer-name').value,
        phone: document.getElementById('customer-phone').value,
        email: document.getElementById('customer-email').value,
        address: document.getElementById('customer-address').value,
        afm: document.getElementById('customer-afm').value,
        notes: document.getElementById('customer-notes').value
    };

    try {
        if (editingCustomerId) {
            await updateCustomer(editingCustomerId, customer);
        } else {
            await createCustomer(customer);
        }
        closeCustomerModal();
        loadCustomers();
    } catch (error) {
        console.error('Error saving customer:', error);
        showError('Αποτυχία αποθήκευσης πελάτη');
    }
}

async function createCustomer(customer) {
    const response = await fetch(CUSTOMERS_API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customer)
    });
    if (!response.ok) throw new Error('Failed to create customer');
    return response.json();
}

async function updateCustomer(id, customer) {
    const response = await fetch(`${CUSTOMERS_API}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customer)
    });
    if (!response.ok) throw new Error('Failed to update customer');
    return response.json();
}

async function editCustomer(id) {
    try {
        const response = await fetch(`${CUSTOMERS_API}/${id}`);
        if (!response.ok) throw new Error('Failed to fetch customer');

        const customer = await response.json();
        document.getElementById('customer-name').value = customer.name;
        document.getElementById('customer-phone').value = customer.phone || '';
        document.getElementById('customer-email').value = customer.email || '';
        document.getElementById('customer-address').value = customer.address || '';
        document.getElementById('customer-afm').value = customer.afm || '';
        document.getElementById('customer-notes').value = customer.notes || '';

        editingCustomerId = id;
        document.getElementById('customer-submit-btn').textContent = 'Ενημέρωση';
        document.getElementById('customer-modal-title').textContent = 'Επεξεργασία Πελάτη';
        document.getElementById('customer-modal').style.display = 'flex';
    } catch (error) {
        console.error('Error loading customer:', error);
        showError('Αποτυχία φόρτωσης πελάτη');
    }
}

async function deleteCustomer(id) {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτόν τον πελάτη;')) return;

    try {
        const response = await fetch(`${CUSTOMERS_API}/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete customer');
        loadCustomers();
    } catch (error) {
        console.error('Error deleting customer:', error);
        showError('Αποτυχία διαγραφής πελάτη');
    }
}

function resetCustomerForm() {
    document.getElementById('customer-form').reset();
    editingCustomerId = null;
    document.getElementById('customer-submit-btn').textContent = 'Αποθήκευση';
}

async function viewCustomerReceipts(customerId, customerName) {
    const modal = document.getElementById('customer-receipts-modal');
    const nameElement = document.getElementById('modal-customer-name');
    const listElement = document.getElementById('modal-receipts-list');

    nameElement.textContent = `Αποδείξεις: ${customerName}`;
    listElement.innerHTML = '<p class="loading">Φόρτωση...</p>';
    modal.style.display = 'flex';

    try {
        const response = await fetch(`${RECEIPTS_API}/customer/${customerId}`);
        if (!response.ok) throw new Error('Failed to fetch receipts');

        const receipts = await response.json();

        if (receipts.length === 0) {
            listElement.innerHTML = '<div class="empty-state"><p>Δεν υπάρχουν αποδείξεις για αυτόν τον πελάτη</p></div>';
            return;
        }

        listElement.innerHTML = receipts.map(receipt => `
            <div class="receipt-card">
                <div class="card-header">
                    <div class="card-title">Απόδειξη #${escapeHtml(receipt.receiptNumber)}</div>
                    <div class="card-badge">${formatCurrency(receipt.amount)}</div>
                </div>
                <div class="card-body">
                    <div class="info-item"><strong>Ημερομηνία:</strong> ${formatDate(receipt.date)}</div>
                    <div class="info-item"><strong>Αιτία:</strong> ${escapeHtml(receipt.reason)}</div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading receipts:', error);
        listElement.innerHTML = '<div class="empty-state"><p>Αποτυχία φόρτωσης αποδείξεων</p></div>';
    }
}
