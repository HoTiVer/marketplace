import { renderNavbar } from "./navbar.js";
import { getAuthData } from "./auth/auth.js";
import { fetchWithAuth } from "./auth/auth.js";

let page = 0;
let searchTerm = "";
let categoryId = null;

document.addEventListener("DOMContentLoaded", async () => {
    await renderNavbar(await getAuthData());
    initSearchPage();
});

async function initSearchPage() {
    const params = new URLSearchParams(window.location.search);

    searchTerm = params.get("q");
    categoryId = params.get("category");

    if (categoryId) {
        document.getElementById("searchTitle").textContent =
            `Category results`;
    } else {
        document.getElementById("searchTitle").textContent =
            `Search results for: "${searchTerm}"`;
    }

    await loadProducts(page);

    document.getElementById("prevPage").addEventListener("click", () => {
        if (page > 0) {
            page--;
            loadProducts(page);
        }
    });

    document.getElementById("nextPage").addEventListener("click", () => {
        page++;
        loadProducts(page);
    });
}

async function loadProducts(pageNumber) {
    let url = "";

    if (categoryId) {
        url = `/api/search/product/category/${categoryId}?page=${pageNumber}&size=8`;
    } else {
        url = `/api/search/product?searchTerm=${encodeURIComponent(searchTerm)}&page=${pageNumber}&size=8`;
    }

    const res = await fetchWithAuth(url);

    const resultsContainer = document.getElementById("resultsContainer");
    resultsContainer.innerHTML = "";

    if (!res.ok) {
        resultsContainer.innerHTML = emptyContentMessage();
        document.getElementById("pageInfo").textContent = "No results";
        return;
    }

    const data = await res.json();

    if (!data.content || data.content.length === 0) {
        resultsContainer.innerHTML = emptyContentMessage();
        updatePagination(data);
        return;
    }

    data.content.forEach(p =>
        resultsContainer.appendChild(createProductCard(p))
    );

    updatePagination(data);
}

function emptyContentMessage() {
    return `
        <p class="text-gray-600 col-span-full text-lg text-center">
            No products found
        </p>
    `;
}

function createProductCard(p) {
    const div = document.createElement("div");
    div.className =
        "bg-white p-4 rounded-xl shadow hover:shadow-lg transition flex flex-col text-left";

    div.innerHTML = `
        <h3 class="text-lg font-bold text-gray-900">${p.name}</h3>

        <p class="text-sm text-gray-500 mt-1">${p.categoryName ?? "No category"}</p>

        <p class="text-blue-600 font-semibold mt-3">$${p.price}</p>

        <p class="text-gray-700 text-sm mt-3 line-clamp-3">
            ${p.description ?? "No description"}
        </p>

        <a href="/product/${p.id}"
           class="mt-auto inline-block px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 active:scale-95 transition">
           View
        </a>
    `;
    return div;
}

function updatePagination({ number, totalPages }) {
    if (totalPages === 0) {
        document.getElementById("pageInfo").textContent = "No results";
        document.getElementById("prevPage").disabled = true;
        document.getElementById("nextPage").disabled = true;
        return;
    }

    document.getElementById("pageInfo").textContent =
        `Page ${number + 1} of ${totalPages}`;

    document.getElementById("prevPage").disabled = number === 0;
    document.getElementById("nextPage").disabled = number === totalPages - 1;
}
