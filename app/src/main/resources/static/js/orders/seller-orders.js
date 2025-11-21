import { renderNavbar } from "../navbar.js";
import { getAuthData, fetchWithAuth } from "../auth/auth.js";

let page = 0;
const size = 10;


const statusMap = {
    CREATED: ['CONFIRMED', 'CANCELLED'],
    CONFIRMED: ['IN_TRANSIT', 'CANCELLED'],
    IN_TRANSIT: ['DELIVERED', 'RETURNED'],
    DELIVERED: ['COMPLETED', 'RETURNED'],
    CANCELLED: [],
    RETURNED: [],
    COMPLETED: []
};

let statuses = [];

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
        const res = await fetchWithAuth(`/api/seller/manage-orders?page=${pageNumber}&size=${size}`);
        if (!res.ok) {
            loading.textContent = `Failed to load orders (${res.status})`;
            return;
        }

        const data = await res.json();
        loading.classList.add('hidden');

        const orders = data.orders.content ?? [];
        statuses = data.statuses ?? [];

        if (orders.length === 0) {
            emptyState.classList.remove('hidden');
            pageInfo.textContent = `Page ${(data.orders.number ?? 0) + 1} of ${Math.max(1, data.orders.totalPages ?? 1)}`;
            document.getElementById('prevPage').disabled = (data.orders.number ?? 0) === 0;
            document.getElementById('nextPage').disabled = (data.orders.number ?? 0) >= ((data.orders.totalPages ?? 1) - 1);
            summary.textContent = `Total orders: ${data.orders.totalElements ?? 0}`;
            return;
        }

        orders.forEach(order => container.appendChild(createOrderCard(order)));

        pageInfo.textContent = `Page ${(data.orders.number ?? 0) + 1} of ${Math.max(1, data.orders.totalPages ?? 1)}`;
        document.getElementById('prevPage').disabled = (data.orders.number ?? 0) === 0;
        document.getElementById('nextPage').disabled = (data.orders.number ?? 0) >= ((data.orders.totalPages ?? 1) - 1);
        summary.textContent = `Total orders: ${data.orders.totalElements ?? 0}`;

    } catch (err) {
        loading.textContent = `Error: ${err.message}`;
    }
}

function createOrderCard(o) {
    const wrapper = document.createElement('div');
    wrapper.className = "bg-white p-4 rounded-xl shadow flex flex-col sm:flex-row sm:items-center gap-4";

    const orderDate = o.orderDate ? new Date(o.orderDate).toLocaleString() : '-';
    const deliveryDate = o.deliveryDate ? new Date(o.deliveryDate).toLocaleString() : '-';
    const productName = o.productName ?? `Product #${o.productId}`;
    const status = o.orderStatus ?? 'UNKNOWN';

    const canChange = (statusMap[status] ?? []).length > 0;

    wrapper.innerHTML = `
        <div class="flex-1">
            <div class="flex items-start justify-between gap-4">
                <div>
                    <a href="/product/${o.productId}" class="text-lg font-semibold text-gray-800 hover:underline block">
                        ${escapeHtml(productName)}
                    </a>
                    <div class="text-sm text-gray-600 mt-1">Qty: ${o.quantity} · Total: $${(o.totalPrice ?? 0).toFixed(2)}</div>
                    <div class="text-sm text-gray-500 mt-1">Recipient: ${escapeHtml(o.recipientName)}, ${escapeHtml(o.recipientPhone)}</div>
                    <div class="text-sm text-gray-500">Address: ${escapeHtml(o.deliveryAddress ?? '-')}, ${escapeHtml(o.deliveryCity ?? '')}</div>
                </div>

                <div class="text-right">
                    <div class="text-sm text-gray-500">Ordered: ${orderDate}</div>
                    <div class="text-sm text-gray-500">Delivery: ${deliveryDate}</div>
                </div>
            </div>

            <div class="mt-3">
                ${canChange
        ? `<select class="px-2 py-1 rounded-md text-sm border" id="status-select-${o.orderId}">
                    ${statuses
            .filter(s => statusMap[status]?.includes(s) || s === status)
            .map(s => `<option value="${s}" ${s === status ? 'selected' : ''}>${s}</option>`).join('')}
                   </select>`
        : `<div class="px-2 py-1 rounded-md text-sm font-medium bg-gray-100 text-gray-700">${status}</div>`
    }
            </div>
        </div>
    `;

    if (canChange) {
        const select = wrapper.querySelector(`#status-select-${o.orderId}`);
        select.addEventListener('change', async () => {
            const newStatus = select.value;
            if (!statusMap[status]?.includes(newStatus)) {
                alert('Cannot change to this status');
                select.value = status;
                return;
            }

            try {
                const res = await fetchWithAuth(`/api/seller/manage-orders/${o.orderId}`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ status: newStatus })
                });
                if (res.ok) {
                    alert(`Status updated to ${newStatus}`);
                } else {
                    alert('Failed to update');
                    select.value = status;
                }
            } catch (err) {
                alert('Network error');
                select.value = status;
            }
        });
    }

    return wrapper;
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
