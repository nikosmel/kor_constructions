// Payments management
// API constants are defined in app.js

let editingPaymentId = null;

document.addEventListener('DOMContentLoaded', () => {
    setupPaymentForm();
    loadCustomersForSelect('payment-customer');
    setupAddPaymentButton();
});

// Modal Functions
async function openPaymentModal() {
    resetPaymentForm();
    document.getElementById('payment-modal-title').textContent = 'Νέα Απόδειξη Πληρωμής';
    document.getElementById('payment-modal').style.display = 'flex';

    // Auto-fill next payment number
    await loadNextPaymentNumber();
}

async function loadNextPaymentNumber() {
    try {
        const response = await fetch(`${PAYMENTS_API}/next-number`);
        const nextNumber = await response.text();
        document.getElementById('payment-number').value = nextNumber;
        // Make it read-only to prevent manual editing
        document.getElementById('payment-number').readOnly = true;
    } catch (error) {
        console.error('Error loading next payment number:', error);
        // If error, allow manual entry
        document.getElementById('payment-number').readOnly = false;
    }
}

function closePaymentModal() {
    document.getElementById('payment-modal').style.display = 'none';
    resetPaymentForm();
}

function setupAddPaymentButton() {
    const addBtn = document.getElementById('add-payment-btn');
    if (addBtn) {
        addBtn.addEventListener('click', openPaymentModal);
    }
}

function setupPaymentForm() {
    const form = document.getElementById('payment-form');
    const printBtn = document.getElementById('payment-print-btn');

    form.addEventListener('submit', handlePaymentFormSubmit);
    printBtn.addEventListener('click', printCurrentPayment);

    // Set today's date as default
    document.getElementById('payment-date').valueAsDate = new Date();

    // Auto-fill signature2 when customer is selected
    const customerSelect = document.getElementById('payment-customer');
    if (customerSelect) {
        customerSelect.addEventListener('change', function() {
            const selectedOption = this.options[this.selectedIndex];
            const customerName = selectedOption.getAttribute('data-name');
            if (customerName) {
                document.getElementById('payment-signature2').value = customerName;
            }
        });
    }
}

async function loadPayments() {
    try {
        const response = await fetch(PAYMENTS_API);
        const payments = await response.json();
        displayPayments(payments);
    } catch (error) {
        console.error('Error loading payments:', error);
        showError('Αποτυχία φόρτωσης πληρωμών');
    }
}

function displayPayments(payments) {
    const container = document.getElementById('payments-container');

    if (payments.length === 0) {
        container.innerHTML = '<div class="empty-state"><h3>Δεν υπάρχουν πληρωμές</h3></div>';
        return;
    }

    container.innerHTML = payments.map(payment => `
        <div class="payment-card">
            <div class="card-header">
                <div class="card-title">Πληρωμή #${escapeHtml(payment.paymentNumber)}</div>
                <div class="card-badge">${formatCurrency(payment.amount)}</div>
            </div>
            <div class="card-body">
                <div class="card-info">
                    <div class="info-item"><strong>Πελάτης:</strong> ${escapeHtml(payment.customerName || payment.payeeName)}</div>
                    <div class="info-item"><strong>Ημερομηνία:</strong> ${formatDate(payment.date)}</div>
                    <div class="info-item"><strong>Αιτία:</strong> ${escapeHtml(payment.reason)}</div>
                </div>
            </div>
            <div class="card-actions">
                <button class="btn btn-print" onclick="printPayment(${payment.id})">Εκτύπωση</button>
                <button class="btn btn-edit" onclick="editPayment(${payment.id})">Επεξεργασία</button>
                <button class="btn btn-delete" onclick="deletePayment(${payment.id})">Διαγραφή</button>
            </div>
        </div>
    `).join('');
}

async function handlePaymentFormSubmit(e) {
    e.preventDefault();

    const select = document.getElementById('payment-customer');
    const selectedOption = select.options[select.selectedIndex];

    const payment = {
        customerId: parseInt(select.value),
        customerName: selectedOption.getAttribute('data-name'),
        date: document.getElementById('payment-date').value,
        paymentNumber: document.getElementById('payment-number').value,
        amount: parseFloat(document.getElementById('payment-amount').value),
        reason: document.getElementById('payment-reason').value,
        signature1: document.getElementById('payment-signature1').value,
        signature2: document.getElementById('payment-signature2').value
    };

    try {
        if (editingPaymentId) {
            await updatePayment(editingPaymentId, payment);
        } else {
            await createPayment(payment);
        }
        closePaymentModal();
        loadPayments();
    } catch (error) {
        console.error('Error saving payment:', error);
        showError('Αποτυχία αποθήκευσης πληρωμής');
    }
}

async function createPayment(payment) {
    const response = await fetch(PAYMENTS_API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payment)
    });
    if (!response.ok) throw new Error('Failed to create payment');
    return response.json();
}

async function updatePayment(id, payment) {
    const response = await fetch(`${PAYMENTS_API}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payment)
    });
    if (!response.ok) throw new Error('Failed to update payment');
    return response.json();
}

async function editPayment(id) {
    try {
        const response = await fetch(`${PAYMENTS_API}/${id}`);
        const payment = await response.json();

        document.getElementById('payment-customer').value = payment.customerId;
        document.getElementById('payment-date').value = payment.date;
        document.getElementById('payment-number').value = payment.paymentNumber;
        document.getElementById('payment-amount').value = payment.amount;
        document.getElementById('payment-reason').value = payment.reason;
        document.getElementById('payment-signature1').value = payment.signature1 || '';
        document.getElementById('payment-signature2').value = payment.signature2 || '';

        editingPaymentId = id;
        document.getElementById('payment-submit-btn').textContent = 'Ενημέρωση';
        document.getElementById('payment-modal-title').textContent = 'Επεξεργασία Απόδειξης Πληρωμής';
        document.getElementById('payment-modal').style.display = 'flex';
    } catch (error) {
        console.error('Error loading payment:', error);
        showError('Αποτυχία φόρτωσης πληρωμής');
    }
}

async function deletePayment(id) {
    if (!confirm('Διαγραφή πληρωμής;')) return;

    try {
        const response = await fetch(`${PAYMENTS_API}/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete payment');
        loadPayments();
    } catch (error) {
        console.error('Error deleting payment:', error);
        showError('Αποτυχία διαγραφής πληρωμής');
    }
}

function resetPaymentForm() {
    document.getElementById('payment-form').reset();
    document.getElementById('payment-date').valueAsDate = new Date();
    editingPaymentId = null;
    document.getElementById('payment-submit-btn').textContent = 'Αποθήκευση';
    // Prefill signature1 with company name
    document.getElementById('payment-signature1').value = 'Korovesis Development';
}

async function printPayment(id) {
    try {
        const response = await fetch(`${PAYMENTS_API}/${id}`);
        const payment = await response.json();
        generatePrintablePayment(payment);
        window.print();
    } catch (error) {
        console.error('Error printing payment:', error);
        showError('Αποτυχία εκτύπωσης');
    }
}

function printCurrentPayment() {
    const select = document.getElementById('payment-customer');
    const selectedOption = select.options[select.selectedIndex];

    const payment = {
        customerName: selectedOption.getAttribute('data-name'),
        date: document.getElementById('payment-date').value,
        paymentNumber: document.getElementById('payment-number').value,
        amount: parseFloat(document.getElementById('payment-amount').value),
        reason: document.getElementById('payment-reason').value,
        signature1: document.getElementById('payment-signature1').value,
        signature2: document.getElementById('payment-signature2').value
    };

    generatePrintablePayment(payment);
    window.print();
}

function generatePrintablePayment(payment) {
    // Clear both print templates to avoid showing multiple receipts
    document.getElementById('print-receipt-template').innerHTML = '';
    document.getElementById('print-payment-template').innerHTML = '';

    const template = document.getElementById('print-payment-template');
    template.innerHTML = `
        <div class="print-payment">
            <div class="print-header">
                <div class="print-title">ΑΠΟΔΕΙΞΗ ΠΛΗΡΩΜΗΣ</div>
                <div class="print-company">KOROVESIS DEVELOPMENT</div>
            </div>
            <div class="print-info">
                <div class="print-row"><strong>Αριθμός:</strong> <span>${escapeHtml(payment.paymentNumber)}</span></div>
                <div class="print-row"><strong>Ημερομηνία:</strong> <span>${formatDate(payment.date)}</span></div>
                <div class="print-row"><strong>Πελάτης:</strong> <span>${escapeHtml(payment.customerName || payment.payeeName)}</span></div>
                <div class="print-row"><strong>Αιτία:</strong> <span>${escapeHtml(payment.reason)}</span></div>
            </div>
            <div class="print-amount">ΠΟΣΟ: ${formatCurrency(payment.amount)}</div>
            <div class="print-signatures">
                <div class="signature-box">
                    <div class="signature-line">${escapeHtml(payment.signature1 || '')}</div>
                    <div>Υπογραφή 1</div>
                </div>
                <div class="signature-box">
                    <div class="signature-line">${escapeHtml(payment.signature2 || '')}</div>
                    <div>Υπογραφή 2</div>
                </div>
            </div>
        </div>
    `;
}
