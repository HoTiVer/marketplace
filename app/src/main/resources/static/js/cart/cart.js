import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const user = await getAuthData();
    await renderNavbar(user);

    const loading = document.getElementById("loading");
    const container = document.getElementById("cartContainer");
    const totalSec = document.getElementById("totalSection");
    const totalPriceEl = document.getElementById("totalPrice");

    async function loadCart() {
        try {
            const res = await fetchWithAuth("/api/cart");
            const items = await res.json();

            loading.classList.add("hidden");
            container.innerHTML = "";

            if (items.length === 0) {
                container.innerHTML = `<p class="text-center text-gray-600 italic">Cart is empty 😢</p>`;
                totalSec.classList.add("hidden");
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
                        <p class="font-semibold text-gray-800">${item.productName}</p>
                        <p class="text-gray-600">$${item.price.toFixed(2)}</p>
                    </div>

                    <div class="flex items-center gap-2">
                        <button class="minus bg-gray-300 px-2 rounded">-</button>
                        <span class="w-8 text-center">${item.quantity}</span>
                        <button class="plus bg-gray-300 px-2 rounded">+</button>
                    </div>

                    <button class="delete bg-red-500 text-white px-3 py-2 rounded-lg hover:bg-red-600 transition">🗑</button>
                `;

                row.querySelector(".plus").onclick = () => updateCount(item.productId, item.quantity + 1);
                row.querySelector(".minus").onclick = () => {
                    if (item.quantity > 1)
                        updateCount(item.productId, item.quantity - 1);
                };
                row.querySelector(".delete").onclick = () => deleteItem(item.productId);

                container.appendChild(row);
            });

            totalPriceEl.textContent = total.toFixed(2);
            totalSec.classList.remove("hidden");
            container.classList.remove("hidden");

        } catch (err) {
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
        await fetchWithAuth(`/api/cart/${productId}`, {
            method: "DELETE"
        });
        await loadCart();
    }

    await loadCart();
});
