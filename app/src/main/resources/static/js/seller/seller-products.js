import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const loading = document.getElementById("loading");
    const productsContainer = document.getElementById("productsContainer");
    const productList = document.getElementById("productList");
    const sellerNameEl = document.getElementById("sellerName");

    const pathParts = window.location.pathname.split("/").filter(Boolean);
    const sellerNickname = pathParts[pathParts.length - 2];

    if (!sellerNickname) {
        loading.textContent = "❌ Seller nickname not provided";
        return;
    }

    try {
        const response = await fetchWithAuth(`/api/v1/seller/${sellerNickname}/products`);

        if (!response.ok) {
            loading.textContent = `❌ Failed to load products (${response.status})`;
            return;
        }

        const products = await response.json();

        loading.classList.add("hidden");
        productsContainer.classList.remove("hidden");
        sellerNameEl.textContent = `Products by ${sellerNickname}`;

        if (!products || products.length === 0) {
            productList.innerHTML = `
                <p class="text-gray-500 italic text-center w-full">
                    No products available
                </p>`;
            return;
        }

        productList.innerHTML = "";

        products.forEach(product => {
            const card = document.createElement("div");
            card.className =
                "bg-white rounded-xl shadow-md overflow-hidden flex flex-col hover:shadow-lg transition";

            const imageSrc = product.mainImageUrl
                ? product.mainImageUrl
                : "/images/default-product.png";

            card.innerHTML = `
                <img 
                    src="${imageSrc}" 
                    alt="${product.productName}"
                    class="w-full h-40 object-cover"
                >

                <div class="p-4 flex flex-col flex-1">
                    <h3 class="text-lg font-semibold text-gray-800 mb-1 line-clamp-2">
                        ${product.productName}
                    </h3>

                    <p class="text-gray-600 mb-3">
                        💰 $${product.price.toFixed(2)}
                    </p>

                    <a 
                        href="/product/${product.productId}" 
                        class="mt-auto text-indigo-600 font-medium hover:underline text-sm"
                    >
                        View Product →
                    </a>
                </div>
            `;

            productList.appendChild(card);
        });

    } catch (err) {
        loading.textContent = `⚠️ Error: ${err.message}`;
    }
});
