import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const sellerContainer = document.getElementById("sellerContainer");
    const loading = document.getElementById("loading");
    const viewProductsBtn = document.getElementById("viewProductsBtn");

    const pathParts = window.location.pathname.split("/").filter(Boolean);
    const sellerNickname = pathParts[pathParts.length - 1];

    if (!sellerNickname) {
        loading.textContent = "❌ Seller nickname not provided";
        return;
    }

    try {
        const response = await fetchWithAuth(`/api/seller/${sellerNickname}`);

        if (!response.ok) {
            loading.textContent = `❌ Failed to load seller (${response.status})`;
            return;
        }

        const seller = await response.json();
        loading.classList.add("hidden");
        sellerContainer.classList.remove("hidden");

        document.getElementById("displayName").textContent = seller.displayName || "—";
        document.getElementById("nickname").textContent = seller.nickname || "—";
        document.getElementById("rating").textContent = seller.rating?.toFixed(2) ?? "0.00";
        document.getElementById("profileDescription").textContent = seller.profileDescription || "No description provided.";

        viewProductsBtn.href = `/seller/${sellerNickname}/products`;

    } catch (err) {
        loading.textContent = `⚠️ Error: ${err.message}`;
    }
});
