import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const user = await getAuthData();
    await renderNavbar(user);

    const loading = document.getElementById("loading");
    const container = document.getElementById("productsContainer");

    async function loadProducts() {
        try {
            // ⚠️ Здесь я предполагаю, что /api/product/all возвращает все продукты продавца.
            // Если у тебя нет такого, нужно добавить или временно использовать другой список.
            const res = await fetchWithAuth("/api/product/all");
            if (!res.ok) {
                loading.textContent = `Failed to load products (${res.status})`;
                return;
            }

            const products = await res.json();
            loading.classList.add("hidden");
            container.classList.remove("hidden");
            container.innerHTML = "";

            if (products.length === 0) {
                container.innerHTML = `
                    <p class="col-span-full text-center text-gray-600 text-lg italic">
                        You have no products yet
                    </p>`;
                return;
            }

            // Сортируем — снизу вверх
            products.reverse();

            products.forEach(p => {
                const card = document.createElement("div");
                card.className =
                    "bg-white rounded-2xl shadow-lg p-6 flex flex-col justify-between hover:shadow-xl transition";

                card.innerHTML = `
                    <div>
                        <h2 class="text-xl font-bold text-gray-800 mb-2">${p.productName}</h2>
                        <p class="text-gray-600 text-lg mb-4">$${p.price.toFixed(2)}</p>
                        <textarea class="w-full border rounded p-2 mb-4">${p.description ?? ""}</textarea>
                    </div>
                    <div class="flex justify-between items-center mt-4">
                        <button class="saveBtn bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition">
                            Save
                        </button>
                        <button class="deleteBtn bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition">
                            Delete
                        </button>
                    </div>
                `;

                const saveBtn = card.querySelector(".saveBtn");
                const deleteBtn = card.querySelector(".deleteBtn");
                const descInput = card.querySelector("textarea");

                saveBtn.onclick = async () => {
                    const newName = prompt("Enter new product name:", p.productName);
                    const newPrice = prompt("Enter new price:", p.price);
                    const newDesc = descInput.value.trim();

                    const updated = {
                        productName: newName,
                        price: parseFloat(newPrice),
                        description: newDesc
                    };

                    const res = await fetchWithAuth(`/api/product/${p.productId}`, {
                        method: "PUT",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(updated)
                    });

                    if (res.ok) {
                        alert("✅ Product updated!");
                        await loadProducts();
                    } else {
                        alert(`Failed to update (${res.status})`);
                    }
                };

                deleteBtn.onclick = async () => {
                    if (!confirm(`Delete "${p.productName}"?`)) return;

                    const res = await fetchWithAuth(`/api/product/${p.productId}`, {
                        method: "DELETE"
                    });

                    if (res.ok) {
                        alert("🗑️ Product deleted!");
                        await loadProducts();
                    } else {
                        alert(`Failed to delete (${res.status})`);
                    }
                };

                container.appendChild(card);
            });
        } catch (err) {
            loading.textContent = `Error: ${err.message}`;
        }
    }

    await loadProducts();
});
