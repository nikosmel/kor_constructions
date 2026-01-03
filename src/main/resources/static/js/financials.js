// API Endpoints
const COMPANY_API = '/api/company';
const FINANCIAL_SUMMARY_API = '/api/company/financial-summary';
const PAYMENTS_API = '/api/payments';
const RECEIPTS_API = '/api/receipts';

// State
let companyData = null;
let allTransactions = [];
let totalExpenses = 0;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    loadFinancialData();
});

function setupEventListeners() {
    // Save settings button
    document.getElementById('save-settings-btn').addEventListener('click', saveSettings);

    // Calculate button
    document.getElementById('calculate-btn').addEventListener('click', calculateCostPerSqm);

    // Transaction filters
    document.getElementById('filter-receipts').addEventListener('change', filterTransactions);
    document.getElementById('filter-payments').addEventListener('change', filterTransactions);
}

// Load all financial data
async function loadFinancialData() {
    try {
        // Load company info, payments, and receipts in parallel
        const [companyRes, paymentsRes, receiptsRes] = await Promise.all([
            fetch(COMPANY_API),
            fetch(PAYMENTS_API),
            fetch(RECEIPTS_API)
        ]);

        if (!companyRes.ok || !paymentsRes.ok || !receiptsRes.ok) {
            throw new Error('Failed to fetch data');
        }

        companyData = await companyRes.json();
        const payments = await paymentsRes.json();
        const receipts = await receiptsRes.json();

        // Populate editable fields
        document.getElementById('starting-capital').value = companyData.startingCapital || '';
        document.getElementById('square-meters').value = companyData.squareMeters || '';

        // Calculate total expenses (sum of all payment amounts)
        totalExpenses = payments.reduce((sum, payment) => sum + parseFloat(payment.amount || 0), 0);
        document.getElementById('total-expenses').value = formatCurrency(totalExpenses);

        // Prepare transactions list
        allTransactions = [
            ...receipts.map(r => ({ ...r, type: 'receipt' })),
            ...payments.map(p => ({ ...p, type: 'payment' }))
        ].sort((a, b) => new Date(b.date) - new Date(a.date));

        filterTransactions();

    } catch (error) {
        console.error('Error loading financial data:', error);
        showError('Αποτυχία φόρτωσης οικονομικών στοιχείων');
    }
}

// Save starting capital and square meters
async function saveSettings() {
    const startingCapital = document.getElementById('starting-capital').value;
    const squareMeters = document.getElementById('square-meters').value;

    // Validation
    if (!startingCapital || parseFloat(startingCapital) < 0) {
        alert('Παρακαλώ εισάγετε έγκυρο αρχικό κεφάλαιο (≥ 0)');
        return;
    }

    if (!squareMeters || parseFloat(squareMeters) <= 0) {
        alert('Παρακαλώ εισάγετε έγκυρα τετραγωνικά μέτρα (> 0)');
        return;
    }

    try {
        // Update company info with new financial fields
        const updatedCompany = {
            ...companyData,
            startingCapital: parseFloat(startingCapital),
            squareMeters: parseFloat(squareMeters)
        };

        const response = await fetch(COMPANY_API, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedCompany)
        });

        if (!response.ok) throw new Error('Failed to update settings');

        companyData = await response.json();
        showSuccess('Οι παράμετροι αποθηκεύτηκαν επιτυχώς!');

    } catch (error) {
        console.error('Error saving settings:', error);
        showError('Αποτυχία αποθήκευσης παραμέτρων');
    }
}

// Calculate cost per square meter
function calculateCostPerSqm() {
    const squareMeters = parseFloat(document.getElementById('square-meters').value);

    if (!squareMeters || squareMeters <= 0) {
        alert('Παρακαλώ εισάγετε και αποθηκεύστε τα τετραγωνικά μέτρα πρώτα');
        return;
    }

    const costPerSqm = totalExpenses / squareMeters;
    document.getElementById('cost-per-sqm').value = formatCurrency(costPerSqm);
}

// Filter and display transactions
function filterTransactions() {
    const showReceipts = document.getElementById('filter-receipts').checked;
    const showPayments = document.getElementById('filter-payments').checked;

    const filtered = allTransactions.filter(t =>
        (t.type === 'receipt' && showReceipts) ||
        (t.type === 'payment' && showPayments)
    );

    displayTransactions(filtered);
}

// Display transactions
function displayTransactions(transactions) {
    const container = document.getElementById('transactions-container');

    if (transactions.length === 0) {
        container.innerHTML = '<div class="empty-state"><h3>Δεν υπάρχουν κινήσεις</h3></div>';
        return;
    }

    container.innerHTML = transactions.map(t => {
        const isReceipt = t.type === 'receipt';
        const name = isReceipt ? t.customerName : t.payeeName;
        const number = isReceipt ? t.receiptNumber : t.paymentNumber;

        return `
            <div class="transaction-item">
                <div class="transaction-type ${isReceipt ? 'type-receipt' : 'type-payment'}">
                    ${isReceipt ? 'Είσπραξη' : 'Πληρωμή'}
                </div>
                <div><strong>${escapeHtml(name)}</strong></div>
                <div>${formatDate(t.date)}</div>
                <div class="${isReceipt ? 'type-receipt' : 'type-payment'}" style="font-weight: 600;">
                    ${isReceipt ? '+' : '-'} ${formatCurrency(t.amount)}
                </div>
                <div style="font-size: 0.9rem; color: #666;">#${escapeHtml(number)} - ${escapeHtml(t.reason)}</div>
            </div>
        `;
    }).join('');
}
