/* Rahul Mart Admin Dashboard JS */

async function initAdminDashboard() {
    const user = await getCurrentUser();
    if (!user || user.role !== 'ADMINISTRATOR') {
        window.location.href = 'login.html';
        return;
    }
    loadAdminUsers();
    loadAdminProducts();
    loadAdminOrders();
}

async function loadAdminUsers() {
    const usersBody = document.getElementById('admin-users-body');
    if (!usersBody) return;

    try {
        const res = await apiCall('/administrator/users');
        const users = res.data;

        usersBody.innerHTML = users.map(u => `
            <tr>
                <td>#${u.id}</td>
                <td style="font-weight: 600;">${escapeHtml(u.fullName)}</td>
                <td>${escapeHtml(u.email)}</td>
                <td><span class="badge badge-${u.role.toLowerCase()}">${u.role}</span></td>
                <td><span class="badge ${u.active ? 'badge-active' : 'badge-inactive'}">${u.active ? 'Active' : 'Deactivated'}</span></td>
                <td>${formatDate(u.createdAt)}</td>
                <td>
                    ${u.role !== 'ADMINISTRATOR' ? `
                        <button onclick="toggleUserStatus(${u.id}, ${!u.active})" class="btn ${u.active ? 'btn-danger' : 'btn-success'} btn-sm">
                            ${u.active ? 'Deactivate' : 'Activate'}
                        </button>
                    ` : '<span style="font-size: 0.8rem; color: var(--text-muted);">Protected</span>'}
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading users: ' + e.message, 'error');
    }
}

async function toggleUserStatus(userId, newActiveState) {
    try {
        await apiCall(`/administrator/users/${userId}/status`, 'PUT', { active: newActiveState });
        showToast('User status updated', 'success');
        loadAdminUsers();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadAdminProducts() {
    const productsBody = document.getElementById('admin-products-body');
    if (!productsBody) return;

    try {
        const res = await apiCall('/administrator/products');
        const products = res.data;

        productsBody.innerHTML = products.map(p => `
            <tr>
                <td>#${p.id}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <img src="${p.imageUrl || 'https://via.placeholder.com/40'}" style="width: 40px; height: 40px; object-fit: cover; border-radius: var(--radius-sm);" />
                        <span style="font-weight: 600;">${escapeHtml(p.name)}</span>
                    </div>
                </td>
                <td>Vendor #${p.vendorId} (${escapeHtml(p.vendorName)})</td>
                <td>${escapeHtml(p.category)}</td>
                <td style="font-weight: 600;">${formatCurrency(p.price)}</td>
                <td>${p.quantity}</td>
                <td><span class="badge ${p.active ? 'badge-active' : 'badge-inactive'}">${p.active ? 'Active' : 'Inactive'}</span></td>
                <td>
                    <button onclick="toggleProductStatusAdmin(${p.id}, ${!p.active})" class="btn ${p.active ? 'btn-danger' : 'btn-success'} btn-sm">
                        ${p.active ? 'Deactivate' : 'Activate'}
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading products: ' + e.message, 'error');
    }
}

async function toggleProductStatusAdmin(productId, newActiveState) {
    try {
        await apiCall(`/administrator/products/${productId}/status`, 'PUT', { active: newActiveState });
        showToast('Product status updated', 'success');
        loadAdminProducts();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadAdminOrders() {
    const ordersBody = document.getElementById('admin-orders-body');
    if (!ordersBody) return;

    try {
        const res = await apiCall('/administrator/orders');
        const orders = res.data;

        ordersBody.innerHTML = orders.map(o => `
            <tr>
                <td style="font-weight: 600;">${o.orderNumber}</td>
                <td>Customer #${o.customerId} (${escapeHtml(o.customerName)})</td>
                <td>${formatDate(o.createdAt)}</td>
                <td style="font-weight: 700;">${formatCurrency(o.totalAmount)}</td>
                <td>
                    <select onchange="updateOrderStatusAdmin(${o.id}, this.value)" class="form-select" style="padding: 0.3rem 0.5rem; font-size: 0.85rem;">
                        <option value="NEW" ${o.status === 'NEW' ? 'selected' : ''}>NEW</option>
                        <option value="CONFIRMED" ${o.status === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                        <option value="PROCESSING" ${o.status === 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
                        <option value="SHIPPED" ${o.status === 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                        <option value="DELIVERED" ${o.status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                        <option value="CANCELLED" ${o.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                    </select>
                </td>
                <td>
                    <button onclick="viewOrderDetailAdmin(${o.id})" class="btn btn-secondary btn-sm">View Details</button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading orders: ' + e.message, 'error');
    }
}

async function updateOrderStatusAdmin(orderId, newStatus) {
    try {
        await apiCall(`/administrator/orders/${orderId}/status`, 'PUT', { status: newStatus });
        showToast(`Order status updated to ${newStatus}`, 'success');
        loadAdminOrders();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function viewOrderDetailAdmin(orderId) {
    const modal = document.getElementById('order-detail-modal');
    const content = document.getElementById('order-detail-content');
    if (!modal || !content) return;

    try {
        const res = await apiCall(`/administrator/orders/${orderId}`);
        const order = res.data;

        content.innerHTML = `
            <div style="margin-bottom: 1rem;">
                <p><strong>Order Number:</strong> ${order.orderNumber}</p>
                <p><strong>Customer:</strong> ${escapeHtml(order.customerName)} (${escapeHtml(order.customerEmail)}) [Customer ID #${order.customerId}]</p>
                <p><strong>Date:</strong> ${formatDate(order.createdAt)}</p>
                <p><strong>Status:</strong> <span class="badge badge-${order.status.toLowerCase()}">${order.status}</span></p>
            </div>
            <h4>Purchased Items</h4>
            <div class="table-container" style="margin-top: 0.5rem;">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Product</th>
                            <th>Vendor</th>
                            <th>Quantity</th>
                            <th>Unit Price</th>
                            <th>Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${order.items.map(item => `
                            <tr>
                                <td>${escapeHtml(item.productName)} (ID #${item.productId})</td>
                                <td>${escapeHtml(item.vendorName)} (Vendor ID #${item.vendorId})</td>
                                <td>${item.quantity}</td>
                                <td>${formatCurrency(item.unitPrice)}</td>
                                <td><strong>${formatCurrency(item.subtotal)}</strong></td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
            <div style="text-align: right; font-size: 1.2rem; font-weight: 700; margin-top: 1rem; color: var(--primary);">
                Grand Total: ${formatCurrency(order.totalAmount)}
            </div>
        `;
        modal.style.display = 'flex';
    } catch (e) {
        showToast('Error loading order details: ' + e.message, 'error');
    }
}

function closeAdminModal() {
    const modal = document.getElementById('order-detail-modal');
    if (modal) modal.style.display = 'none';
}

function switchTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.style.display = 'none');

    document.getElementById(`tab-${tabName}`).style.display = 'block';
    event.target.classList.add('active');
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('admin-users-body')) {
        initAdminDashboard();
    }
});
