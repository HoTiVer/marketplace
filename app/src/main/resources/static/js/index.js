import { renderNavbar } from "./navbar.js";
import { getAuthData } from "./auth/auth.js";
import { fetchWithAuth } from "./auth/auth.js";

document.addEventListener("DOMContentLoaded", async () => {
    const authData = await getAuthData();
    await renderNavbar(authData);


    const res = await fetchWithAuth("/api/home");
    if (!res.ok) {
        console.error("Failed to load home data:", res.status);
        return;
    }
    const data = await res.json();


    const categoriesList = document.getElementById("categoriesList");
    data.categories.forEach(cat => {
        const li = document.createElement("li");
        li.textContent = cat.name;
        li.className = "px-3 py-2 rounded-lg hover:bg-blue-100 cursor-pointer";
        li.onclick = () => {
            window.location.href = `/search.html?category=${cat.id}`;
        }
        categoriesList.appendChild(li);
    });

    function renderProducts(containerId, products) {
        const container = document.getElementById(containerId);
        products.forEach(p => {
            const card = document.createElement("div");
            card.className = "bg-white rounded-xl shadow p-4 flex flex-col gap-2 hover:shadow-lg transition";
            card.innerHTML = `
                <h3 class="font-semibold text-lg">${p.productName}</h3>
                <p class="text-blue-600 font-bold">$${p.price.toFixed(2)}</p>
                <button class="mt-auto bg-green-600 text-white px-3 py-2 rounded-lg hover:bg-green-700 transition">
                    View
                </button>

            `;
            card.querySelector("button").onclick = () => {
                window.location.href = `/product/${p.productId}`;
            };
            container.appendChild(card);
        });
    }

    renderProducts("featuredProducts", data.featuredProducts);
    renderProducts("newProducts", data.newProducts);
    renderProducts("popularProducts", data.popularProducts);
});
