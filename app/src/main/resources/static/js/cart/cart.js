import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const user = await getAuthData();
    await renderNavbar(user);

    const loading = document.getElementById("loading");
    const container = document.getElementById("cartContainer");
    const totalSec = document.getElementById("totalSection");
    const totalPriceEl = document.getElementById("totalPrice");

    const orderCard = document.getElementById("orderCard");
    const openOrderCardBtn = document.getElementById("openOrderCardBtn");
    const closeOrderCardBtn = document.getElementById("closeOrderCard");
    const orderStatus = document.getElementById("orderStatus");

    async function loadCart() {
        try {
            const res = await fetchWithAuth("/api/cart");
            const items = await res.json();

            loading.classList.add("hidden");
            container.innerHTML = "";

            if (!items.length) {
                container.innerHTML =
                    `<p class="text-center text-gray-600 italic">Cart is empty 😢</p>`;
                totalSec.classList.add("hidden");
                openOrderCardBtn.classList.add("hidden");
                container.classList.remove("hidden");
                return;
            }

            let total = 0;

            items.forEach(item => {
                total += item.price * item.quantity;

                const row = document.createElement("div");
                row.className =
                    "flex justify-between items-center bg-gray-50 p-4 rounded-xl shadow";

                row.innerHTML = `
                    <div>
                        <p 
                            class="product-link font-semibold text-blue-600 hover:underline cursor-pointer"
                            data-id="${item.productId}">
                            ${item.productName}
                        </p>
                        <p class="text-gray-600">$${item.price.toFixed(2)}</p>
                    </div>

                    <div class="flex items-center gap-2">
                        <input
                            type="number"
                            min="1"
                            class="quantity-input w-16 border rounded text-center p-1"
                            value="${item.quantity}"
                            data-id="${item.productId}"
                        />
                    </div>

                    <button class="delete bg-red-500 text-white px-3 py-2 rounded-lg hover:bg-red-600 transition">
                        🗑
                    </button>
                `;

                row.querySelector(".product-link").onclick = () => {
                    window.location.href = `/product/${item.productId}`;
                };

                const qtyInput = row.querySelector(".quantity-input");

                const saveQuantity = async () => {
                    let value = Number(qtyInput.value);

                    if (!value || value < 1) {
                        qtyInput.value = item.quantity;
                        return;
                    }

                    if (value === item.quantity) return;

                    await updateCount(item.productId, value);
                };

                qtyInput.addEventListener("blur", saveQuantity);
                qtyInput.addEventListener("keydown", e => {
                    if (e.key === "Enter") {
                        qtyInput.blur();
                    }
                });

                row.querySelector(".delete").onclick =
                    () => deleteItem(item.productId);

                container.appendChild(row);
            });

            totalPriceEl.textContent = total.toFixed(2);
            totalSec.classList.remove("hidden");
            container.classList.remove("hidden");
            openOrderCardBtn.classList.remove("hidden");

        } catch {
            loading.textContent = "Failed to load cart";
        }
    }

    async function updateCount(productId, count) {
        await fetchWithAuth(`/api/cart/${productId}?count=${count}`, {
            method: "PATCH"
        });
        await loadCart();
    }

    async function deleteItem(productId) {
        await fetchWithAuth(`/api/cart/${productId}`, { method: "DELETE" });
        await loadCart();
    }

    function validateOrderForm(body) {
        const nameRegex = /^[A-Za-zА-Яа-яЇїІіЄє\s]{2,}$/;
        const phoneRegex = /^[0-9+\-\s]{7,15}$/;

        if (!nameRegex.test(body.receiverName)) {
            return "Invalid receiver name";
        }
        if (!phoneRegex.test(body.receiverPhone)) {
            return "Invalid phone number";
        }
        if (!body.deliveryCity || body.deliveryCity.length < 2) {
            return "City is required";
        }
        if (!body.deliveryAddress || body.deliveryAddress.length < 5) {
            return "Address is required";
        }
        return null;
    }

    async function createOrder() {
        orderStatus.textContent = "";

        const body = {
            receiverName: document.getElementById("receiverName").value.trim(),
            receiverPhone: document.getElementById("receiverPhone").value.trim(),
            deliveryCity: document.getElementById("deliveryCity").value.trim(),
            deliveryAddress: document.getElementById("deliveryAddress").value.trim()
        };

        const validationError = validateOrderForm(body);
        if (validationError) {
            orderStatus.textContent = validationError;
            orderStatus.className = "text-red-600 font-semibold";
            return;
        }

        try {
            const res = await fetchWithAuth("/api/order", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            if (res.ok) {
                orderStatus.textContent = "Order created successfully!";
                orderStatus.className = "text-green-600 font-semibold";
                orderCard.classList.add("hidden");
                await loadCart();
            } else {
                let errorMessage = "Failed to create order";

                try {
                    const data = await res.json();
                    if (data.message) {
                        errorMessage = data.message;
                    }
                } catch {
                    errorMessage = await res.text();
                }

                orderStatus.textContent = errorMessage;
                orderStatus.className = "text-red-600 font-semibold";
            }
        } catch (err) {
            orderStatus.textContent = err.message;
            orderStatus.className = "text-red-600 font-semibold";
        }
    }

    openOrderCardBtn.onclick = () => orderCard.classList.remove("hidden");
    closeOrderCardBtn.onclick = () => orderCard.classList.add("hidden");
    document.getElementById("createOrderBtn").onclick = createOrder;

    await loadCart();
});
