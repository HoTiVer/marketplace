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
    const fileInput = document.getElementById("productImage");

    const imagesSection = document.getElementById("imagesSection");
    const imagesGrid = document.getElementById("imagesGrid");

    let editingProduct = null;

    /* ================== LOAD CATEGORIES ================== */
    async function loadCategories() {
        const res = await fetchWithAuth("/api/v1/category");
        const categories = await res.json();
        categorySelect.innerHTML = `<option value="">Select category...</option>`;
        categories.forEach(cat => {
            const opt = document.createElement("option");
            opt.value = cat.name;
            opt.textContent = cat.name;
            categorySelect.appendChild(opt);
        });
    }

    /* ================== LOAD PRODUCTS ================== */
    async function loadProducts() {
        const res = await fetchWithAuth("/api/v1/seller/products");
        const products = await res.json();

        loading.classList.add("hidden");
        container.classList.remove("hidden");
        container.innerHTML = "";

        if (products.length === 0) {
            container.innerHTML =
                `<p class="col-span-full text-center text-gray-600 italic text-lg">
                    You have no products yet.
                </p>`;
            return;
        }

        products.forEach(prod => {
            const card = document.createElement("div");
            card.className =
                "bg-white rounded-2xl shadow-lg p-6 flex flex-col justify-between hover:shadow-xl transition cursor-pointer";

            const imgSrc = prod.mainImageUrl ?? "/images/default-product.png";

            card.innerHTML = `
                <div>
                    <img src="${imgSrc}" class="w-full h-40 object-cover rounded-xl mb-3">
                    <h2 class="text-xl font-bold text-gray-800 mb-2">${prod.name}</h2>
                    <p class="text-gray-600 mb-2">Category: ${prod.categoryName}</p>
                    <p class="text-lg text-blue-700 font-semibold mb-4">$${prod.price.toFixed(2)}</p>
                    <p class="text-gray-700 text-sm mb-4 line-clamp-3">${prod.description}</p>
                    <p class="text-gray-600 font-medium">Quantity: ${prod.quantity}</p>
                </div>
                <div class="flex justify-between items-center mt-2">
                    <button class="editBtn bg-yellow-500 text-white px-3 py-2 rounded-lg hover:bg-yellow-600">
                        Edit
                    </button>
                    <button class="deleteBtn bg-red-500 text-white px-3 py-2 rounded-lg hover:bg-red-600">
                        Delete
                    </button>
                </div>
            `;

            card.onclick = e => {
                if (!e.target.classList.contains("editBtn") &&
                    !e.target.classList.contains("deleteBtn")) {
                    window.location.href = `/api/v1/product/${prod.id}`;
                }
            };

            card.querySelector(".editBtn").onclick = e => {
                e.stopPropagation();
                openModal(prod);
            };

            card.querySelector(".deleteBtn").onclick = async e => {
                e.stopPropagation();
                if (!confirm(`Delete "${prod.name}"?`)) return;
                const resp = await fetchWithAuth(`/api/v1/product/${prod.id}`, { method: "DELETE" });
                if (resp.ok) loadProducts();
                else alert(`Failed (${resp.status})`);
            };

            container.appendChild(card);
        });
    }

    /* ================== MODAL ================== */
    async function openModal(product = null) {
        modal.classList.remove("hidden");
        modalTitle.textContent = product ? "Edit Product" : "Add Product";

        form.reset();
        charContainer.innerHTML = "";
        fileInput.value = "";

        imagesSection.classList.add("hidden");
        imagesGrid.innerHTML = "";

        if (product) {
            const res = await fetchWithAuth(`/api/v1/seller/products/${product.id}`);
            editingProduct = await res.json();

            productName.value = editingProduct.name;
            productPrice.value = editingProduct.price;
            productDescription.value = editingProduct.description;
            productQuantity.value = editingProduct.quantity ?? 0;
            categorySelect.value = editingProduct.categoryName;

            if (editingProduct.characteristic) {
                Object.entries(editingProduct.characteristic)
                    .forEach(([k, v]) => addCharacteristicRow(k, v));
            }

            renderImages(editingProduct);
        } else {
            editingProduct = null;
        }
    }

    function closeModal() {
        modal.classList.add("hidden");
        editingProduct = null;
    }

    cancelBtn.onclick = closeModal;

    /* ================== CHARACTERISTICS ================== */
    function addCharacteristicRow(key = "", value = "") {
        const div = document.createElement("div");
        div.className = "flex gap-2 items-center";
        div.innerHTML = `
            <input type="text" value="${key}" placeholder="Key" class="flex-1 border px-3 py-2 rounded-lg">
            <input type="text" value="${value}" placeholder="Value" class="flex-1 border px-3 py-2 rounded-lg">
            <button type="button" class="removeBtn text-red-500">✖</button>
        `;
        div.querySelector(".removeBtn").onclick = () => div.remove();
        charContainer.appendChild(div);
    }

    addCharBtn.onclick = () => addCharacteristicRow();

    /* ================== IMAGES ================== */
    function renderImages(product) {
        if (!product.images || product.images.length === 0) return;

        imagesSection.classList.remove("hidden");
        imagesGrid.innerHTML = "";

        product.images.forEach(img => {
            const div = document.createElement("div");
            div.className = "relative group rounded-xl overflow-hidden shadow";

            div.innerHTML = `
                <img src="${img.url}" class="w-full h-40 object-cover">

                ${img.isMain ? `
                    <span class="absolute top-2 left-2 bg-green-600 text-white text-xs px-2 py-1 rounded-full">
                        MAIN
                    </span>
                ` : `
                    <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100
                                flex gap-2 items-center justify-center transition">
                        <button class="makeMainBtn bg-blue-600 text-white px-3 py-1 rounded-lg text-sm">
                            ⭐ Main
                        </button>
                        <button class="deleteImgBtn bg-red-600 text-white px-3 py-1 rounded-lg text-sm">
                            🗑 Delete
                        </button>
                    </div>
                `}
            `;

            if (!img.isMain) {
                div.querySelector(".makeMainBtn").onclick = async () => {
                    await fetchWithAuth(
                        `/api/v1/product/${product.id}/image/${img.id}/main`,
                        { method: "PATCH" }
                    );
                    await reloadEditingProduct(product.id);
                };

                div.querySelector(".deleteImgBtn").onclick = async () => {
                    if (!confirm("Delete this image?")) return;
                    await fetchWithAuth(
                        `/api/v1/product/${product.id}/image/${img.id}`,
                        { method: "DELETE" }
                    );
                    await reloadEditingProduct(product.id);
                };
            }

            imagesGrid.appendChild(div);
        });
    }

    async function reloadEditingProduct(productId) {
        const res = await fetchWithAuth(`/api/v1/seller/product/${productId}`);
        editingProduct = await res.json();
        renderImages(editingProduct);
    }

    /* ================== SUBMIT ================== */
    form.onsubmit = async e => {
        e.preventDefault();

        const characteristic = {};
        charContainer.querySelectorAll("div").forEach(div => {
            const [k, v] = div.querySelectorAll("input");
            if (k.value) characteristic[k.value] = v.value;
        });

        const productData = {
            name: productName.value.trim(),
            price: parseFloat(productPrice.value),
            description: productDescription.value.trim(),
            categoryName: categorySelect.value,
            characteristic,
            quantity: parseInt(productQuantity.value) || 0
        };

        const formData = new FormData();
        formData.append("data", new Blob([JSON.stringify(productData)], { type: "application/json" }));

        if (fileInput.files.length > 0) {
            formData.append("image", fileInput.files[0]);
        }

        const url = editingProduct
            ? `/api/v1/product/${editingProduct.id}`
            : "/api/v1/product";

        const method = editingProduct ? "PUT" : "POST";

        const res = await fetchWithAuth(url, { method, body: formData });

        if (!res.ok) {
            alert(`Error: ${res.status}`);
            return;
        }

        closeModal();
        loadProducts();
    };

    await loadCategories();
    await loadProducts();
    addBtn.onclick = () => openModal();
});
