/* Rahul Mart Cart JS */

async function loadCartPage() {
    const cartContainer = document.getElementById('cart-content');
    if (!cartContainer) return;

    try {
        const res = await apiCall('/basket');
        const cart = res.data;

        if (!cart.items || cart.items.length === 0) {
            cartContainer.innerHTML = `
                <div style="text-align: center; padding: 4rem 1rem;">
                    <h2>Your Shopping Cart is Empty</h2>
                    <p style="color: var(--text-muted); margin-bottom: 1.5rem;">Explore our catalog to find top products from independent vendors.</p>
                    <a href="index.html" class="btn btn-primary">Browse Catalogue</a>
                </div>
            `;
            return;
        }

        let itemsHtml = `
            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem;">
                <div>
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Price</th>
                                    <th>Quantity</th>
                                    <th>Subtotal</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
        `;

        cart.items.forEach(item => {
            const prod = item.product;
            itemsHtml += `
                <tr>
                    <td>
                        <div style="display: flex; align-items: center; gap: 0.85rem;">
                            <img src="${prod.imageUrl || 'https://via.placeholder.com/60'}" style="width: 50px; height: 50px; object-fit: cover; border-radius: var(--radius-sm);" />
                            <div>
                                <a href="product-detail.html?id=${prod.id}" style="font-weight: 600; color: var(--dark);">${escapeHtml(prod.name)}</a>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">Vendor: ${escapeHtml(prod.vendorName || '')}</div>
                            </div>
                        </div>
                    </td>
                    <td>${formatCurrency(prod.price)}</td>
                    <td>
                        <div style="display: flex; align-items: center; gap: 0.5rem;">
                            <button onclick="updateQty(${prod.id}, ${item.quantity - 1})" class="btn btn-secondary btn-sm">-</button>
                            <span style="font-weight: 600; width: 24px; text-align: center;">${item.quantity}</span>
                            <button onclick="updateQty(${prod.id}, ${item.quantity + 1})" class="btn btn-secondary btn-sm">+</button>
                        </div>
                    </td>
                    <td style="font-weight: 700;">${formatCurrency(item.subtotal)}</td>
                    <td>
                        <button onclick="removeItem(${prod.id})" class="btn btn-danger btn-sm">Remove</button>
                    </td>
                </tr>
            `;
        });

        itemsHtml += `
                            </tbody>
                        </table>
                    </div>
                </div>
                <div>
                    <div style="background: #fff; padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); box-shadow: var(--shadow-sm);">
                        <h3 style="margin-bottom: 1.25rem;">Order Summary</h3>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 0.75rem;">
                            <span>Subtotal (${cart.totalItemsCount} items)</span>
                            <span style="font-weight: 600;">${formatCurrency(cart.subtotal)}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between; margin-bottom: 1.25rem; font-size: 1.15rem; font-weight: 700; border-top: 1px solid var(--border-color); padding-top: 0.75rem;">
                            <span>Total</span>
                            <span style="color: var(--primary);">${formatCurrency(cart.total)}</span>
                        </div>
                        <a href="checkout.html" class="btn btn-primary" style="width: 100%;">Proceed to Checkout</a>
                        <button onclick="clearCart()" class="btn btn-secondary btn-sm" style="width: 100%; margin-top: 0.75rem;">Clear Cart</button>
                    </div>
                </div>
            </div>
        `;

        cartContainer.innerHTML = itemsHtml;
    } catch (e) {
        cartContainer.innerHTML = `<div style="color: var(--danger); text-align: center;">Failed to load cart: ${e.message}</div>`;
    }
}

async function updateQty(productId, quantity) {
    try {
        await apiCall(`/basket/items/${productId}`, 'PUT', { quantity });
        renderHeader();
        loadCartPage();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function removeItem(productId) {
    try {
        await apiCall(`/basket/items/${productId}`, 'DELETE');
        showToast('Item removed', 'info');
        renderHeader();
        loadCartPage();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function clearCart() {
    try {
        await apiCall('/basket', 'DELETE');
        showToast('Cart cleared', 'info');
        renderHeader();
        loadCartPage();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('cart-content')) {
        loadCartPage();
    }
});
