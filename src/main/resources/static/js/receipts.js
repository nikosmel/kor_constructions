// Receipts management
const RECEIPTS_API_BASE = '/api/receipts';
const CUSTOMERS_API_BASE = '/api/customers';

let editingReceiptId = null;

document.addEventListener('DOMContentLoaded', () => {
    setupReceiptForm();
    loadCustomersForSelect();
    setupAddReceiptButton();
});

// Modal Functions
function openReceiptModal() {
    resetReceiptForm();
    document.getElementById('receipt-modal-title').textContent = 'Νέα Απόδειξη Είσπραξης';
    document.getElementById('receipt-modal').style.display = 'flex';
}

function closeReceiptModal() {
    document.getElementById('receipt-modal').style.display = 'none';
    resetReceiptForm();
}

function setupAddReceiptButton() {
    const addBtn = document.getElementById('add-receipt-btn');
    if (addBtn) {
        addBtn.addEventListener('click', openReceiptModal);
    }
}

function setupReceiptForm() {
    const form = document.getElementById('receipt-form');
    const printBtn = document.getElementById('receipt-print-btn');

    form.addEventListener('submit', handleReceiptFormSubmit);
    printBtn.addEventListener('click', printCurrentReceipt);

    // Set today's date as default
    document.getElementById('receipt-date').valueAsDate = new Date();
}

async function loadCustomersForSelect() {
    try {
        const response = await fetch(CUSTOMERS_API_BASE);
        const customers = await response.json();
        
        const select = document.getElementById('receipt-customer');
        select.innerHTML = '<option value="">Επιλέξτε πελάτη</option>' +
            customers.map(c => `<option value="${c.id}" data-name="${escapeHtml(c.name)}">${escapeHtml(c.name)}</option>`).join('');
    } catch (error) {
        console.error('Error loading customers:', error);
    }
}

async function loadReceipts() {
    try {
        const response = await fetch(RECEIPTS_API_BASE);
        const receipts = await response.json();
        displayReceipts(receipts);
    } catch (error) {
        console.error('Error loading receipts:', error);
        showError('Αποτυχία φόρτωσης αποδείξεων');
    }
}

function displayReceipts(receipts) {
    const container = document.getElementById('receipts-container');

    if (receipts.length === 0) {
        container.innerHTML = '<div class="empty-state"><h3>Δεν υπάρχουν αποδείξεις</h3></div>';
        return;
    }

    container.innerHTML = receipts.map(receipt => `
        <div class="receipt-card">
            <div class="card-header">
                <div class="card-title">Απόδειξη #${escapeHtml(receipt.receiptNumber)}</div>
                <div class="card-badge">${formatCurrency(receipt.amount)}</div>
            </div>
            <div class="card-body">
                <div class="card-info">
                    <div class="info-item"><strong>Πελάτης:</strong> ${escapeHtml(receipt.customerName)}</div>
                    <div class="info-item"><strong>Ημερομηνία:</strong> ${formatDate(receipt.date)}</div>
                    <div class="info-item"><strong>Αιτία:</strong> ${escapeHtml(receipt.reason)}</div>
                </div>
            </div>
            <div class="card-actions">
                <button class="btn btn-print" onclick="printReceipt(${receipt.id})">Εκτύπωση</button>
                <button class="btn btn-edit" onclick="editReceipt(${receipt.id})">Επεξεργασία</button>
                <button class="btn btn-delete" onclick="deleteReceipt(${receipt.id})">Διαγραφή</button>
            </div>
        </div>
    `).join('');
}

async function handleReceiptFormSubmit(e) {
    e.preventDefault();

    const select = document.getElementById('receipt-customer');
    const selectedOption = select.options[select.selectedIndex];

    const receipt = {
        customerId: parseInt(select.value),
        customerName: selectedOption.getAttribute('data-name'),
        date: document.getElementById('receipt-date').value,
        receiptNumber: document.getElementById('receipt-number').value,
        amount: parseFloat(document.getElementById('receipt-amount').value),
        reason: document.getElementById('receipt-reason').value,
        signature1: document.getElementById('receipt-signature1').value,
        signature2: document.getElementById('receipt-signature2').value
    };

    try {
        if (editingReceiptId) {
            await updateReceipt(editingReceiptId, receipt);
        } else {
            await createReceipt(receipt);
        }
        closeReceiptModal();
        loadReceipts();
    } catch (error) {
        console.error('Error saving receipt:', error);
        showError('Αποτυχία αποθήκευσης απόδειξης');
    }
}

async function createReceipt(receipt) {
    const response = await fetch(RECEIPTS_API_BASE, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(receipt)
    });
    if (!response.ok) throw new Error('Failed to create receipt');
    return response.json();
}

async function updateReceipt(id, receipt) {
    const response = await fetch(`${RECEIPTS_API_BASE}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(receipt)
    });
    if (!response.ok) throw new Error('Failed to update receipt');
    return response.json();
}

async function editReceipt(id) {
    try {
        const response = await fetch(`${RECEIPTS_API_BASE}/${id}`);
        const receipt = await response.json();

        document.getElementById('receipt-customer').value = receipt.customerId;
        document.getElementById('receipt-date').value = receipt.date;
        document.getElementById('receipt-number').value = receipt.receiptNumber;
        document.getElementById('receipt-amount').value = receipt.amount;
        document.getElementById('receipt-reason').value = receipt.reason;
        document.getElementById('receipt-signature1').value = receipt.signature1 || '';
        document.getElementById('receipt-signature2').value = receipt.signature2 || '';

        editingReceiptId = id;
        document.getElementById('receipt-submit-btn').textContent = 'Ενημέρωση';
        document.getElementById('receipt-modal-title').textContent = 'Επεξεργασία Απόδειξης Είσπραξης';
        document.getElementById('receipt-modal').style.display = 'flex';
    } catch (error) {
        console.error('Error loading receipt:', error);
        showError('Αποτυχία φόρτωσης απόδειξης');
    }
}

async function deleteReceipt(id) {
    if (!confirm('Διαγραφή απόδειξης;')) return;

    try {
        const response = await fetch(`${RECEIPTS_API_BASE}/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete receipt');
        loadReceipts();
    } catch (error) {
        console.error('Error deleting receipt:', error);
        showError('Αποτυχία διαγραφής απόδειξης');
    }
}

function resetReceiptForm() {
    document.getElementById('receipt-form').reset();
    document.getElementById('receipt-date').valueAsDate = new Date();
    editingReceiptId = null;
    document.getElementById('receipt-submit-btn').textContent = 'Αποθήκευση';
}

async function printReceipt(id) {
    try {
        const response = await fetch(`${RECEIPTS_API_BASE}/${id}`);
        const receipt = await response.json();
        generatePrintableReceipt(receipt);
        window.print();
    } catch (error) {
        console.error('Error printing receipt:', error);
        showError('Αποτυχία εκτύπωσης');
    }
}

function printCurrentReceipt() {
    const select = document.getElementById('receipt-customer');
    const selectedOption = select.options[select.selectedIndex];
    
    const receipt = {
        customerName: selectedOption.getAttribute('data-name'),
        date: document.getElementById('receipt-date').value,
        receiptNumber: document.getElementById('receipt-number').value,
        amount: parseFloat(document.getElementById('receipt-amount').value),
        reason: document.getElementById('receipt-reason').value,
        signature1: document.getElementById('receipt-signature1').value,
        signature2: document.getElementById('receipt-signature2').value
    };
    
    generatePrintableReceipt(receipt);
    window.print();
}

function generatePrintableReceipt(receipt) {
    const template = document.getElementById('print-receipt-template');
    template.innerHTML = `
        <div class="print-receipt">
            <div class="print-header">
                <div class="print-title">ΑΠΟΔΕΙΞΗ ΕΙΣΠΡΑΞΗΣ</div>
                <div class="print-company">KOR CONSTRUCTIONS</div>
            </div>
            <div class="print-info">
                <div class="print-row"><strong>Αριθμός:</strong> <span>${escapeHtml(receipt.receiptNumber)}</span></div>
                <div class="print-row"><strong>Ημερομηνία:</strong> <span>${formatDate(receipt.date)}</span></div>
                <div class="print-row"><strong>Πελάτης:</strong> <span>${escapeHtml(receipt.customerName)}</span></div>
                <div class="print-row"><strong>Αιτία:</strong> <span>${escapeHtml(receipt.reason)}</span></div>
            </div>
            <div class="print-amount">ΠΟΣΟ: ${formatCurrency(receipt.amount)}</div>
            <div class="print-signatures">
                <div class="signature-box">
                    <div class="signature-line">${escapeHtml(receipt.signature1 || '')}</div>
                    <div>Υπογραφή 1</div>
                </div>
                <div class="signature-box">
                    <div class="signature-line">${escapeHtml(receipt.signature2 || '')}</div>
                    <div>Υπογραφή 2</div>
                </div>
            </div>
        </div>
    `;
}
