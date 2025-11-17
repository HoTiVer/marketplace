import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const user = await getAuthData();
    await renderNavbar(user);

    const loading = document.getElementById("loading");
    const container = document.getElementById("productsContainer");
    const addBtn = document.getElementById("addProductBtn");
    const modal = document.getElementById("productModal");
    const form = document.getElementById("productForm");
    const modalTitle = document.getElementById("modalTitle");
    const cancelBtn = document.getElementById("cancelBtn");
    const categorySelect = document.getElementById("categorySelect");
    const charContainer = document.getElementById("characteristicsContainer");
    const addCharBtn = document.getElementById("addCharacteristic");

    let editingProduct = null;

    async function loadCategories() {
        const res = await fetchWithAuth("/api/category");
        const categories = await res.json();

        categorySelect.innerHTML = `<option value="">Select category...</option>`;
        categories.forEach(cat => {
            const opt = document.createElement("option");
            opt.value = cat.name;
            opt.textContent = cat.name;
            categorySelect.appendChild(opt);
        });
    }

    async function loadProducts() {
        const res = await fetchWithAuth("/api/seller/products");
        const products = await res.json();
        loading.classList.add("hidden");
        container.classList.remove("hidden");
        container.innerHTML = "";

        if (products.length === 0) {
            container.innerHTML = `<p class="col-span-full text-center text-gray-600 italic text-lg">You have no products yet.</p>`;
            return;
        }

        for (const prod of products) {
            const card = document.createElement("div");
            card.className =
                "bg-white rounded-2xl shadow-lg p-6 flex flex-col justify-between hover:shadow-xl transition";

            card.innerHTML = `
                <div>
                    <h2 class="text-xl font-bold text-gray-800 mb-2">${prod.name}</h2>
                    <p class="text-gray-600 mb-2">Category: ${prod.categoryName}</p>
                    <p class="text-lg text-blue-700 font-semibold mb-4">$${prod.price.toFixed(2)}</p>
                    <p class="text-gray-700 text-sm mb-4 line-clamp-3">${prod.description}</p>
                    <p class="text-gray-600 font-medium">Quantity: ${prod.quantity}</p>
                </div>
                <div class="flex justify-between items-center mt-2">
                    <button class="editBtn bg-yellow-500 text-white px-3 py-2 rounded-lg hover:bg-yellow-600 transition">Edit</button>
                    <button class="deleteBtn bg-red-500 text-white px-3 py-2 rounded-lg hover:bg-red-600 transition">Delete</button>
                </div>
            `;

            card.querySelector(".editBtn").onclick = () => openModal(prod);

            card.querySelector(".deleteBtn").onclick = async () => {
                if (!confirm(`Delete "${prod.name}"?`)) return;
                const resp = await fetchWithAuth(`/api/product/${prod.id}`, { method: "DELETE" });
                if (resp.ok) loadProducts();
                else alert(`Failed (${resp.status})`);
            };

            container.appendChild(card);
        }
    }

    function openModal(product = null) {
        editingProduct = product;
        modal.classList.remove("hidden");
        modalTitle.textContent = product ? "Edit Product" : "Add Product";
        form.reset();
        charContainer.innerHTML = "";
        document.getElementById("productQuantity").value = 0;

        if (product) {
            document.getElementById("productName").value = product.name;
            document.getElementById("productPrice").value = product.price;
            document.getElementById("productDescription").value = product.description;
            document.getElementById("productQuantity").value = product.quantity ?? 0;
            categorySelect.value = product.categoryName;

            if (product.characteristic) {
                for (const [key, value] of Object.entries(product.characteristic)) {
                    addCharacteristicRow(key, value);
                }
            }
        }
    }

    function closeModal() {
        modal.classList.add("hidden");
        editingProduct = null;
    }

    cancelBtn.onclick = closeModal;

    function addCharacteristicRow(key = "", value = "") {
        const div = document.createElement("div");
        div.className = "flex gap-2 items-center";
        div.innerHTML = `
          <input type="text" value="${key}" placeholder="Key" class="flex-1 border px-3 py-2 rounded-lg">
          <input type="text" value="${value}" placeholder="Value" class="flex-1 border px-3 py-2 rounded-lg">
          <button type="button" class="removeBtn text-red-500 hover:text-red-700">✖</button>
        `;
        div.querySelector(".removeBtn").onclick = () => div.remove();
        charContainer.appendChild(div);
    }

    addCharBtn.onclick = () => addCharacteristicRow();

    form.onsubmit = async e => {
        e.preventDefault();

        const characteristic = {};
        charContainer.querySelectorAll("div").forEach(div => {
            const [keyInput, valueInput] = div.querySelectorAll("input");
            if (keyInput.value) {
                characteristic[keyInput.value] = valueInput.value;
            }
        });


        const body = {
            name: form.productName.value.trim(),
            price: parseFloat(form.productPrice.value),
            description: form.productDescription.value.trim(),
            categoryName: categorySelect.value,
            characteristic,
            quantity: parseInt(document.getElementById("productQuantity").value) || 0
        };

        const url = editingProduct ? `/api/product/${editingProduct.id}` : "/api/product";
        const method = editingProduct ? "PUT" : "POST";

        const res = await fetchWithAuth(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            alert(`Error: ${res.status}`);
            return;
        }

        closeModal();
        await loadProducts();
    };

    await loadCategories();
    await loadProducts();
    addBtn.onclick = () => openModal();
});
