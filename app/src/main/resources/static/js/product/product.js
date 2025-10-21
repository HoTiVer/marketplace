import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const productContainer = document.getElementById("productContainer");
    const loading = document.getElementById("loading");

    const pathParts = window.location.pathname.split("/").filter(Boolean);
    const productId = Number(pathParts[pathParts.length - 1]);

    if (!productId) {
        loading.textContent = "❌ Product ID not provided";
        return;
    }

    try {
        const response = await fetchWithAuth(`/api/product/${productId}`);

        if (!response.ok) {
            loading.textContent = `❌ Failed to load product (${response.status})`;
            return;
        }

        const product = await response.json();
        loading.classList.add("hidden");
        productContainer.classList.remove("hidden");

        // === Заполняем поля ===
        document.getElementById("productName").textContent = product.name;
        document.getElementById("price").textContent = `$${product.price.toFixed(2)}`;
        document.getElementById("categoryName").textContent = `Category: ${product.categoryName}`;
        document.getElementById("description").textContent = product.description;

        document.getElementById("sellerDisplayName").textContent = product.sellerDisplayName;
        document.getElementById("sellerUsername").textContent = product.sellerUsername;

        // === Характеристики ===
        const charContainer = document.getElementById("characteristics");
        if (product.characteristic && Object.keys(product.characteristic).length > 0) {
            Object.entries(product.characteristic).forEach(([key, value]) => {
                const div = document.createElement("div");
                div.className = "bg-gray-50 border rounded-lg p-4";
                div.innerHTML = `
          <p class="text-gray-800 font-medium">${key}</p>
          <p class="text-gray-600">${value}</p>
        `;
                charContainer.appendChild(div);
            });
        } else {
            charContainer.innerHTML = `<p class="text-gray-500 italic">No characteristics available</p>`;
        }

    } catch (err) {
        loading.textContent = `⚠️ Error: ${err.message}`;
    }
});
