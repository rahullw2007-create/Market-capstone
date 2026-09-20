/* Rahul Mart Vendor Portal JS */

let currentVendorProducts = [];

async function initVendorDashboard() {
    const user = await getCurrentUser();
    if (!user || user.role !== 'VENDOR') {
        window.location.href = 'login.html';
        return;
    }
    loadVendorProducts();
    loadVendorOrders();
}

async function loadVendorProducts() {
    const tableBody = document.getElementById('vendor-products-body');
    if (!tableBody) return;

    try {
        const res = await apiCall('/vendor/products');
        currentVendorProducts = res.data;

        if (currentVendorProducts.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No products listed yet. Click "Add New Product" to create your first product!</td></tr>`;
            return;
        }

        tableBody.innerHTML = currentVendorProducts.map(p => `
            <tr>
                <td>#${p.id}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <img src="${p.imageUrl || 'https://via.placeholder.com/40'}" style="width: 40px; height: 40px; object-fit: cover; border-radius: var(--radius-sm);" />
                        <span style="font-weight: 600;">${escapeHtml(p.name)}</span>
                    </div>
                </td>
                <td>${escapeHtml(p.category)}</td>
                <td style="font-weight: 600;">${formatCurrency(p.price)}</td>
                <td>
                    <span style="font-weight: 600; color: ${p.quantity > 5 ? 'var(--dark)' : 'var(--danger)'};">${p.quantity}</span>
                </td>
                <td>
                    <span class="badge ${p.active ? 'badge-active' : 'badge-inactive'}">${p.active ? 'Active' : 'Inactive'}</span>
                </td>
                <td>
                    <div style="display: flex; gap: 0.5rem;">
                        <button onclick="openProductModal(${p.id})" class="btn btn-secondary btn-sm">Edit</button>
                        ${p.active ? 
                            `<button onclick="toggleProductStatus(${p.id})" class="btn btn-danger btn-sm">Deactivate</button>` : 
                            `<button onclick="reactivateProduct(${p.id})" class="btn btn-success btn-sm">Activate</button>`
                        }
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading products: ' + e.message, 'error');
    }
}

async function loadVendorOrders() {
    const ordersBody = document.getElementById('vendor-orders-body');
    if (!ordersBody) return;

    try {
        const res = await apiCall('/vendor/orders');
        const orders = res.data;

        if (orders.length === 0) {
            ordersBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No orders containing your products yet.</td></tr>`;
            return;
        }

        ordersBody.innerHTML = orders.map(o => `
            <tr>
                <td style="font-weight: 600;">${o.orderNumber}</td>
                <td>${escapeHtml(o.customerName)} (${escapeHtml(o.customerEmail)})</td>
                <td>${formatDate(o.createdAt)}</td>
                <td>
                    <ul style="list-style: none; padding: 0;">
                        ${o.items.map(item => `
                            <li style="font-size: 0.85rem; margin-bottom: 0.25rem;">
                                <strong>${escapeHtml(item.productName)}</strong> × ${item.quantity} (${formatCurrency(item.unitPrice)}) = <strong>${formatCurrency(item.subtotal)}</strong>
                            </li>
                        `).join('')}
                    </ul>
                </td>
                <td><span class="badge badge-${o.status.toLowerCase()}">${o.status}</span></td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading orders: ' + e.message, 'error');
    }
}

function openProductModal(productId = null) {
    const modal = document.getElementById('product-modal');
    const form = document.getElementById('product-form');
    if (!modal || !form) return;

    form.reset();
    document.getElementById('modal-title').textContent = productId ? 'Edit Product' : 'Add New Product';
    document.getElementById('productId').value = productId || '';

    if (productId) {
        const prod = currentVendorProducts.find(p => p.id === productId);
        if (prod) {
            document.getElementById('name').value = prod.name;
            document.getElementById('category').value = prod.category;
            document.getElementById('price').value = prod.price;
            document.getElementById('quantity').value = prod.quantity;
            document.getElementById('imageUrl').value = prod.imageUrl;
            document.getElementById('description').value = prod.description;
        }
    }

    modal.style.display = 'flex';
}

function closeProductModal() {
    const modal = document.getElementById('product-modal');
    if (modal) modal.style.display = 'none';
}

async function handleProductSubmit(event) {
    event.preventDefault();
    const id = document.getElementById('productId').value;
    const name = document.getElementById('name').value;
    const category = document.getElementById('category').value;
    const price = parseFloat(document.getElementById('price').value);
    const quantity = parseInt(document.getElementById('quantity').value);
    const imageUrl = document.getElementById('imageUrl').value;
    const description = document.getElementById('description').value;

    const payload = { name, category, price, quantity, imageUrl, description, active: true };

    try {
        if (id) {
            await apiCall(`/vendor/products/${id}`, 'PUT', payload);
            showToast('Product updated successfully', 'success');
        } else {
            await apiCall('/vendor/products', 'POST', payload);
            showToast('Product created successfully', 'success');
        }
        closeProductModal();
        loadVendorProducts();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function toggleProductStatus(id) {
    if (!confirm('Are you sure you want to deactivate this product?')) return;
    try {
        await apiCall(`/vendor/products/${id}`, 'DELETE');
        showToast('Product deactivated', 'info');
        loadVendorProducts();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function reactivateProduct(id) {
    const prod = currentVendorProducts.find(p => p.id === id);
    if (!prod) return;
    try {
        prod.active = true;
        await apiCall(`/vendor/products/${id}`, 'PUT', prod);
        showToast('Product reactivated', 'success');
        loadVendorProducts();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('vendor-products-body')) {
        initVendorDashboard();
    }
});
