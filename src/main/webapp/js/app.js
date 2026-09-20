/* Rahul Mart App Core JS */

// Works locally at /zenith-bazaar/ and in cloud deployments where the WAR is ROOT.
const API_BASE = new URL('api', document.baseURI).pathname.replace(/\/$/, '');

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

async function apiCall(endpoint, method = 'GET', body = null) {
    const cleanEndpoint = endpoint.startsWith('/api') ? endpoint.substring(4) : endpoint;
    const normalizedEndpoint = cleanEndpoint.startsWith('/') ? cleanEndpoint : `/${cleanEndpoint}`;
    const url = `${API_BASE}${normalizedEndpoint}`;

    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        credentials: 'same-origin'
    };

    if (body !== null) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);
    const contentType = response.headers.get('content-type') || '';

    if (!contentType.toLowerCase().includes('application/json')) {
        throw new Error(`Server returned an unexpected response (HTTP ${response.status}).`);
    }

    const data = await response.json();

    if (!response.ok || !data.success) {
        throw new Error(data.message || `Request failed with status ${response.status}`);
    }

    return data;
}

async function getCurrentUser() {
    try {
        const res = await apiCall('/account/me');
        return res.data;
    } catch (e) {
        return null;
    }
}

function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount || 0);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

async function renderHeader() {
    const headerNav = document.getElementById('navbar-links');
    if (!headerNav) return;

    const user = await getCurrentUser();
    let cartCount = 0;

    if (user && user.role === 'CUSTOMER') {
        try {
            const cartRes = await apiCall('/basket');
            cartCount = cartRes.data.totalItemsCount || 0;
        } catch (e) {}
    }

    let linksHtml = `<li><a href="index.html">Shop</a></li>`;

    if (user) {
        if (user.role === 'CUSTOMER') {
            linksHtml += `
                <li><a href="cart.html">Cart <span class="badge badge-new">${cartCount}</span></a></li>
                <li><a href="orders.html">My Orders</a></li>
            `;
        } else if (user.role === 'VENDOR') {
            linksHtml += `<li><a href="vendor-dashboard.html">Vendor Portal</a></li>`;
        } else if (user.role === 'ADMINISTRATOR') {
            linksHtml += `<li><a href="admin-dashboard.html">Admin Dashboard</a></li>`;
        }

        linksHtml += `
            <li class="nav-user">Hi, ${escapeHtml(user.fullName)} <span class="nav-role">${escapeHtml(user.role)}</span></li>
            <li><button onclick="handleLogout()" class="btn btn-secondary btn-sm">Logout</button></li>
        `;
    } else {
        linksHtml += `
            <li><a href="login.html">Login</a></li>
            <li><a href="register.html" class="btn btn-primary btn-sm" style="color: #fff;">Create account</a></li>
        `;
    }

    headerNav.innerHTML = linksHtml;
}

async function handleLogout() {
    try {
        await apiCall('/account/logout', 'POST');
        showToast('Logged out successfully', 'success');
        setTimeout(() => window.location.href = 'index.html', 500);
    } catch (e) {
        showToast('Logout failed: ' + e.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    renderHeader();
});
