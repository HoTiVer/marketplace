import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const productContainer = document.getElementById("productContainer");
    const loading = document.getElementById("loading");

    const addToWishlistBtn = document.getElementById("addToWishlistBtn");
    const addToCartBtn = document.getElementById("addToCartBtn");
    const goToSellerPageBtn = document.getElementById("goToSellerPageBtn");
    const goToSellerProductsBtn = document.getElementById("goToSellerProductsBtn");
    const goToReviewsBtn = document.getElementById("goToReviewsBtn");

    if (!userData) {
        addToWishlistBtn.classList.add("hidden");
        addToCartBtn.classList.add("hidden");
    }

    const pathParts = window.location.pathname.split("/").filter(Boolean);
    const productId = Number(pathParts[pathParts.length - 1]);
    if (!productId) {
        loading.textContent = "Product ID not provided";
        return;
    }

    try {
        const response = await fetchWithAuth(`/api/product/${productId}`);
        if (!response.ok) {
            loading.textContent = `Failed to load product (${response.status})`;
            return;
        }

        const product = await response.json();
        loading.classList.add("hidden");
        productContainer.classList.remove("hidden");

        document.getElementById("productName").textContent = product.name;
        document.getElementById("price").textContent = `$${product.price.toFixed(2)}`;
        document.getElementById("categoryName").textContent = `Category: ${product.categoryName}`;
        document.getElementById("description").textContent = product.description;

        document.getElementById("sellerDisplayName").textContent = product.sellerDisplayName;
        document.getElementById("sellerUsername").textContent = product.sellerUsername;

        if (product.sellerUsername) {
            goToSellerPageBtn.classList.remove("hidden");
            goToSellerProductsBtn.classList.remove("hidden");

            goToSellerPageBtn.onclick = () =>
                window.location.href = `/seller/${product.sellerUsername}`;

            goToSellerProductsBtn.onclick = () =>
                window.location.href = `/seller/${product.sellerUsername}/products`;
        }

        if (userData) {
            addToWishlistBtn.onclick = async () => {
                try {
                    const res = await fetchWithAuth(`/api/wishlist/${productId}`, { method: "POST" });
                    if (res.ok) alert(`✅ "${product.name}" added to wishlist!`);
                    else if (res.status === 409) alert("Already in wishlist.");
                    else alert(`Failed to add to wishlist (${res.status})`);
                } catch (err) {
                    alert(`Error: ${err.message}`);
                }
            };

            addToCartBtn.onclick = async () => {
                try {
                    let res = await fetchWithAuth(`/api/cart/${productId}?count=1`, { method: "POST" });
                    if (res.ok) {
                        alert(`🛒 "${product.name}" added to cart!`);
                        return;
                    }
                    if (res.status === 409) {
                        res = await fetchWithAuth(`/api/cart/${productId}?count=1`, { method: "PATCH" });
                        if (res.ok) alert(`🛒 Quantity of "${product.name}" increased by 1!`);
                        else alert(`Failed to update cart (${res.status})`);
                    } else {
                        alert(`Failed to add to cart (${res.status})`);
                    }
                } catch (err) {
                    alert(`Error: ${err.message}`);
                }
            };
        }

        goToReviewsBtn.onclick = () => {
            window.location.href = `/product/${productId}/reviews`;
        };

        const charContainer = document.getElementById("characteristics");
        charContainer.innerHTML = "";
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
            charContainer.innerHTML =
                `<p class="text-gray-500 italic">No characteristics available</p>`;
        }

        const mainImage = document.getElementById("mainImage");
        const thumbnails = document.getElementById("thumbnails");
        thumbnails.innerHTML = "";

        if (product.images && product.images.length > 0) {
            const mainImg = product.images.find(img => img.isMain) || product.images[0];
            mainImage.src = mainImg.url;

            product.images.forEach(img => {
                const thumb = document.createElement("img");
                thumb.src = img.url;
                thumb.alt = product.name;
                thumb.className = "w-24 h-24 object-cover rounded-lg shadow cursor-pointer border-2 border-transparent hover:border-blue-500 transition";
                thumb.onclick = () => { mainImage.src = img.url; };
                thumbnails.appendChild(thumb);
            });
        } else {
            mainImage.src = "";
            thumbnails.innerHTML = `<p class="text-gray-500 italic">No images available</p>`;
        }

    } catch (err) {
        loading.textContent = `Error: ${err.message}`;
    }
});
