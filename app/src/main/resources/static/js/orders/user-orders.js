import { renderNavbar } from "../navbar.js";
import { getAuthData, fetchWithAuth } from "../auth/auth.js";

let page = 0;
const size = 10;

const cancellableStatuses = new Set(['CREATED', 'CONFIRMED']);

document.addEventListener("DOMContentLoaded", async () => {
    const auth = await getAuthData();
    await renderNavbar(auth);

    document.getElementById('prevPage').addEventListener('click', () => {
        if (page > 0) {
            page--;
            loadOrders(page);
        }
    });
    document.getElementById('nextPage').addEventListener('click', () => {
        page++;
        loadOrders(page);
    });

    await loadOrders(page);
});

async function loadOrders(pageNumber = 0) {
    const loading = document.getElementById('loading');
    const container = document.getElementById('ordersContainer');
    const emptyState = document.getElementById('emptyState');
    const pageInfo = document.getElementById('pageInfo');
    const summary = document.getElementById('summary');

    loading.classList.remove('hidden');
    container.innerHTML = '';
    emptyState.classList.add('hidden');
    summary.textContent = 'Loading orders...';

    try {
        const res = await fetchWithAuth(`/cabinet/orders?page=${pageNumber}&size=${size}`);
        if (!res.ok) {
            loading.textContent = `Failed to load orders (${res.status})`;
            return;
        }

        const data = await res.json();

        loading.classList.add('hidden');

        const orders = data.content ?? [];
        if (orders.length === 0) {
            emptyState.classList.remove('hidden');
            pageInfo.textContent = `Page ${ (data.number ?? 0) + 1 } of ${ Math.max(1, data.totalPages ?? 1) }`;
            document.getElementById('prevPage').disabled = (data.number ?? 0) === 0;
            document.getElementById('nextPage').disabled = (data.number ?? 0) >= ((data.totalPages ?? 1) - 1);
            summary.textContent = `Total orders: ${data.totalElements ?? 0}`;
            return;
        }

        orders.forEach(order => {
            const card = createOrderCard(order);
            container.appendChild(card);
        });

        pageInfo.textContent = `Page ${ (data.number ?? 0) + 1 } of ${ Math.max(1, data.totalPages ?? 1) }`;
        document.getElementById('prevPage').disabled = (data.number ?? 0) === 0;
        document.getElementById('nextPage').disabled = (data.number ?? 0) >= ((data.totalPages ?? 1) - 1);

        summary.textContent = `Total orders: ${data.totalElements ?? 0}`;

    } catch (err) {
        loading.textContent = `Error: ${err.message}`;
    }
}

function createOrderCard(o) {
    const wrapper = document.createElement('div');
    wrapper.className = "bg-white p-4 rounded-xl shadow flex flex-col sm:flex-row sm:items-center gap-4";

    const orderDate = o.orderDate ? formatDate(o.orderDate) : '-';
    const deliveryDate = o.deliveryDate ? formatDate(o.deliveryDate) : '-';
    const status = o.orderStatus ?? 'UNKNOWN';
    const productName = o.productName ?? `Product #${o.productId}`;

    wrapper.innerHTML = `
        <div class="flex-1">
            <div class="flex items-start justify-between gap-4">
                <div>
                    <a href="/product/${o.productId}" class="text-lg font-semibold text-gray-800 hover:underline block">
                        ${escapeHtml(productName)}
                    </a>
                    <div class="text-sm text-gray-600 mt-1">Qty: ${o.quantity} · Total: $${(o.totalPrice ?? 0).toFixed(2)}</div>
                </div>

                <div class="text-right">
                    <div class="text-sm text-gray-500">Ordered: ${orderDate}</div>
                    <div class="text-sm text-gray-500">Delivery: ${deliveryDate}</div>
                </div>
            </div>

            <div class="mt-3 flex items-center gap-3">
                <div class="px-2 py-1 rounded-md text-sm font-medium ${statusBadgeClass(status)}" id="status-${o.orderId}">
                    ${status}
                </div>
                <div class="text-sm text-gray-500">Address: ${escapeHtml(o.deliveryAddress ?? '-')}</div>
            </div>

            <!-- Expanded details section -->
            <div class="mt-2 text-sm text-gray-700 hidden" id="details-section-${o.orderId}">
                <p>Seller: <a href="/seller/${o.sellerNickname}" class="text-blue-600 hover:underline">${o.sellerNickname}</a></p>
                <p>Order Date: ${orderDate}</p>
                <p>Delivery Date: ${deliveryDate}</p>
                <p>Quantity: ${o.quantity}</p>
                <p>Total Price: $${(o.totalPrice ?? 0).toFixed(2)}</p>
                <p>Delivery Address: ${escapeHtml(o.deliveryAddress ?? '-')}</p>
            </div>
        </div>

        <div class="flex flex-col items-stretch sm:items-end gap-2">
            <div class="flex gap-2 mt-2">
                <button class="px-3 py-2 rounded-lg bg-white border hover:bg-gray-50 text-sm" id="details-${o.orderId}">
                    Details
                </button>
                <button class="px-3 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 text-sm hidden" id="cancel-${o.orderId}">
                    Cancel
                </button>
            </div>
        </div>
    `;

    const cancelBtn = wrapper.querySelector(`#cancel-${o.orderId}`);
    const detailsBtn = wrapper.querySelector(`#details-${o.orderId}`);
    const detailsSection = wrapper.querySelector(`#details-section-${o.orderId}`);

    if (cancellableStatuses.has(status)) {
        cancelBtn.classList.remove('hidden');
    }

    detailsBtn.addEventListener('click', () => {
        detailsSection.classList.toggle('hidden');
        detailsBtn.textContent = detailsSection.classList.contains('hidden') ? 'Details' : 'Hide';
    });

    cancelBtn.addEventListener('click', async () => {
        const ok = confirm(`Cancel this order? This action cannot be undone.`);
        if (!ok) return;
        cancelBtn.disabled = true;
        cancelBtn.textContent = 'Cancelling...';

        try {
            const res = await fetchWithAuth(`/cabinet/orders/${o.orderId}/cancel`, { method: 'PATCH' });
            if (res.ok) {
                const statusEl = wrapper.querySelector(`#status-${o.orderId}`);
                statusEl.textContent = 'CANCELLED';
                statusEl.className = 'px-2 py-1 rounded-md text-sm font-medium bg-gray-200 text-gray-700';
                cancelBtn.remove();
            } else {
                const txt = await res.text().catch(() => res.status);
                alert(`Failed to cancel: ${txt}`);
                cancelBtn.disabled = false;
                cancelBtn.textContent = 'Cancel';
            }
        } catch (err) {
            alert(`Network error: ${err.message}`);
            cancelBtn.disabled = false;
            cancelBtn.textContent = 'Cancel';
        }
    });

    return wrapper;
}

function formatDate(iso) {
    try {
        const d = new Date(iso);
        return d.toLocaleString();
    } catch {
        return iso;
    }
}

function statusBadgeClass(status) {

    switch ((status ?? '').toUpperCase()) {
        case 'CREATED': return 'bg-yellow-100 text-yellow-800';
        case 'CONFIRMED': return 'bg-blue-100 text-blue-800';
        case 'IN_TRANSIT': return 'bg-indigo-100 text-indigo-800';
        case 'DELIVERED': return 'bg-green-100 text-green-800';
        case 'CANCELLED': return 'bg-gray-100 text-gray-700';
        case 'RETURNED': return 'bg-red-100 text-red-800';
        case 'COMPLETED': return 'bg-green-200 text-green-900';
        default: return 'bg-gray-100 text-gray-700';
    }
}


function escapeHtml(s) {
    if (!s) return '';
    return String(s)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
