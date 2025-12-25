// Main application file - handles tab switching and initialization

document.addEventListener('DOMContentLoaded', () => {
    initializeTabs();
    initializeModals();
    loadAllData();
    initializeDropboxSync();
});

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
                    if (typeof loadCustomersForSelect === 'function') loadCustomersForSelect();
                    break;
                case 'payments':
                    if (typeof loadPayments === 'function') loadPayments();
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
    const closeBtn = modal.querySelector('.modal-close');

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

function initializeDropboxSync() {
    const syncBtn = document.getElementById('sync-dropbox-btn');
    if (!syncBtn) return;

    syncBtn.addEventListener('click', async () => {
        // Disable button during sync
        syncBtn.disabled = true;
        syncBtn.textContent = '🔄 Συγχρονισμός...';

        try {
            const response = await fetch('/api/backup/restore', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (data.success) {
                alert('✅ Συγχρονισμός επιτυχής!\n\n' + data.message);
                // Reload current tab data
                loadAllData();
                // Reload all tabs if they have load functions
                if (typeof loadCustomers === 'function') loadCustomers();
                if (typeof loadReceipts === 'function') loadReceipts();
                if (typeof loadPayments === 'function') loadPayments();
                if (typeof loadTransactions === 'function') loadTransactions();
            } else {
                alert('⚠️ Συγχρονισμός αποτυχία:\n\n' + data.message);
            }
        } catch (error) {
            console.error('Sync error:', error);
            alert('❌ Σφάλμα κατά τον συγχρονισμό:\n\n' + error.message);
        } finally {
            // Re-enable button
            syncBtn.disabled = false;
            syncBtn.textContent = '📥 Συγχρονισμός Dropbox';
        }
    });
}
