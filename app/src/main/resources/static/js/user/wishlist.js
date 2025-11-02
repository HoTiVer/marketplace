import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const main = document.querySelector("main");
    const wishlistContainer = document.getElementById("wishlistContainer");

    main.style.opacity = "0";

    try {
        const [userData, response] = await Promise.all([
            getAuthData(),
            fetchWithAuth("/api/wishlist")
        ]);

        await renderNavbar(userData);

        if (!response.ok) {
            wishlistContainer.innerHTML = `
                <p class="col-span-full text-center text-gray-600 text-lg italic">
                    Failed to load wishlist (${response.status})
                </p>`;
            main.style.opacity = "1";
            return;
        }

        const wishlist = await response.json();

        wishlistContainer.innerHTML = "";

        if (wishlist.length === 0) {
            wishlistContainer.innerHTML = `
                <p class="col-span-full text-center text-gray-600 text-lg italic">
                    Your wishlist is empty
                </p>`;
        } else {

            wishlist.reverse().forEach(product => {
                const card = document.createElement("div");
                card.className =
                    "bg-white rounded-2xl shadow-lg p-6 flex flex-col justify-between hover:shadow-xl transition";

                card.innerHTML = `
                    <div>
                        <h2 class="text-xl font-bold text-gray-800 mb-2">${product.productName}</h2>
                        <p class="text-gray-600 text-lg mb-4">$${product.price.toFixed(2)}</p>
                    </div>
                    <div class="flex justify-between items-center mt-4">
                        <button
                            class="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition">
                            View
                        </button>
                        <button
                            class="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition">
                            Remove
                        </button>
                    </div>
                `;

                const [viewBtn, removeBtn] = card.querySelectorAll("button");

                viewBtn.onclick = () => {
                    window.location.href = `/product/${product.productId}`;
                };

                removeBtn.onclick = async () => {
                    if (!confirm(`Remove "${product.productName}" from wishlist?`)) return;

                    try {
                        const delResponse = await fetchWithAuth(`/api/wishlist/${product.productId}`, {
                            method: "DELETE"
                        });
                        if (!delResponse.ok) {
                            alert(`Failed to remove (${delResponse.status})`);
                            return;
                        }

                        location.reload();
                    } catch (err) {
                        alert(`Error: ${err.message}`);
                    }
                };

                wishlistContainer.appendChild(card);
            });
        }

        main.style.opacity = "1";
    } catch (err) {
        wishlistContainer.innerHTML = `
            <p class="col-span-full text-center text-gray-600 text-lg italic">
                Error: ${err.message}
            </p>`;
        main.style.opacity = "1";
    }
});
