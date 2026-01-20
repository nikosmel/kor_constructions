// Main application file - handles tab switching and initialization

// Shared API endpoints
const CUSTOMERS_API = '/api/customers';
const RECEIPTS_API = '/api/receipts';
const PAYMENTS_API = '/api/payments';

document.addEventListener('DOMContentLoaded', () => {
    initializeTabs();
    initializeModals();
    loadAllData();
});

// Shared function to load customers into a select element
async function loadCustomersForSelect(selectId) {
    try {
        const response = await fetch(CUSTOMERS_API);
        const customers = await response.json();

        const select = document.getElementById(selectId);
        if (select) {
            select.innerHTML = '<option value="">Επιλέξτε πελάτη</option>' +
                customers.map(c => `<option value="${c.id}" data-name="${escapeHtml(c.name)}">${escapeHtml(c.name)}</option>`).join('');
        }
    } catch (error) {
        console.error('Error loading customers:', error);
    }
}

function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabName = button.getAttribute('data-tab');

            // Remove active class from all buttons and contents
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            // Add active class to clicked button and corresponding content
            button.classList.add('active');
            document.getElementById(`${tabName}-tab`).classList.add('active');

            // Reload data when switching to a tab
            switch(tabName) {
                case 'customers':
                    if (typeof loadCustomers === 'function') loadCustomers();
                    break;
                case 'receipts':
                    if (typeof loadReceipts === 'function') loadReceipts();
                    loadCustomersForSelect('receipt-customer');
                    break;
                case 'payments':
                    if (typeof loadPayments === 'function') loadPayments();
                    loadCustomersForSelect('payment-customer');
                    break;
                case 'transactions':
                    if (typeof loadTransactions === 'function') loadTransactions();
                    break;
            }
        });
    });
}

function initializeModals() {
    const modal = document.getElementById('customer-receipts-modal');
    if (!modal) return; // Modal doesn't exist on all pages

    const closeBtn = modal.querySelector('.modal-close');
    if (!closeBtn) return;

    closeBtn.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });
}

function loadAllData() {
    // Load initial data for the active tab
    if (typeof loadCustomers === 'function') {
        loadCustomers();
    }
}

// Utility functions
function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('el-GR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return 'N/A';
    const date = new Date(dateTimeStr);
    return date.toLocaleString('el-GR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('el-GR', {
        style: 'currency',
        currency: 'EUR'
    }).format(amount);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showError(message) {
    alert(message);
}

function showSuccess(message) {
    // Could be replaced with a toast notification
    console.log('Success:', message);
}

