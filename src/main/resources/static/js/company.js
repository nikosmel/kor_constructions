// Company Info management
const COMPANY_API = '/api/company';

document.addEventListener('DOMContentLoaded', () => {
    setupCompanyForm();
    loadCompanyInfo();
});

function setupCompanyForm() {
    const form = document.getElementById('company-form');
    form.addEventListener('submit', handleCompanyFormSubmit);
}

async function loadCompanyInfo() {
    try {
        const response = await fetch(COMPANY_API);
        if (!response.ok) throw new Error('Failed to fetch company info');

        const company = await response.json();
        populateCompanyForm(company);
    } catch (error) {
        console.error('Error loading company info:', error);
        showError('Αποτυχία φόρτωσης πληροφοριών εταιρείας');
    }
}

function populateCompanyForm(company) {
    document.getElementById('company-name').value = company.companyName || '';
    document.getElementById('company-tax-id').value = company.taxId || '';
    document.getElementById('company-doy').value = company.doy || '';
    document.getElementById('company-address').value = company.address || '';
    document.getElementById('company-city').value = company.city || '';
    document.getElementById('company-postal-code').value = company.postalCode || '';
    document.getElementById('company-phone').value = company.phone || '';
    document.getElementById('company-mobile').value = company.mobile || '';
    document.getElementById('company-email').value = company.email || '';
    document.getElementById('company-website').value = company.website || '';
    document.getElementById('company-description').value = company.description || '';
}

async function handleCompanyFormSubmit(e) {
    e.preventDefault();

    const company = {
        companyName: document.getElementById('company-name').value,
        taxId: document.getElementById('company-tax-id').value,
        doy: document.getElementById('company-doy').value,
        address: document.getElementById('company-address').value,
        city: document.getElementById('company-city').value,
        postalCode: document.getElementById('company-postal-code').value,
        phone: document.getElementById('company-phone').value,
        mobile: document.getElementById('company-mobile').value,
        email: document.getElementById('company-email').value,
        website: document.getElementById('company-website').value,
        description: document.getElementById('company-description').value
    };

    try {
        const response = await fetch(COMPANY_API, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(company)
        });

        if (!response.ok) throw new Error('Failed to update company info');

        alert('Οι πληροφορίες εταιρείας ενημερώθηκαν επιτυχώς!');
    } catch (error) {
        console.error('Error updating company info:', error);
        showError('Αποτυχία ενημέρωσης πληροφοριών εταιρείας');
    }
}
