// Transactions view - combines receipts and payments
let allTransactions = [];

document.addEventListener('DOMContentLoaded', () => {
    setupTransactionFilters();
});

function setupTransactionFilters() {
    const receiptFilter = document.getElementById('filter-receipts');
    const paymentFilter = document.getElementById('filter-payments');

    receiptFilter.addEventListener('change', filterTransactions);
    paymentFilter.addEventListener('change', filterTransactions);
}

async function loadTransactions() {
    try {
        const [receiptsRes, paymentsRes] = await Promise.all([
            fetch('/api/receipts'),
            fetch('/api/payments')
        ]);

        const receipts = await receiptsRes.json();
        const payments = await paymentsRes.json();

        // Combine and sort by date
        allTransactions = [
            ...receipts.map(r => ({ ...r, type: 'receipt' })),
            ...payments.map(p => ({ ...p, type: 'payment' }))
        ].sort((a, b) => new Date(b.date) - new Date(a.date));

        filterTransactions();
    } catch (error) {
        console.error('Error loading transactions:', error);
        showError('Αποτυχία φόρτωσης κινήσεων');
    }
}

function filterTransactions() {
    const showReceipts = document.getElementById('filter-receipts').checked;
    const showPayments = document.getElementById('filter-payments').checked;

    const filtered = allTransactions.filter(t =>
        (t.type === 'receipt' && showReceipts) ||
        (t.type === 'payment' && showPayments)
    );

    displayTransactions(filtered);
}

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
