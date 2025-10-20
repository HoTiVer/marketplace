import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const mainContent = document.getElementById("mainContent");
    const userCard = document.getElementById("userCard");

    async function loadUserInfo() {
        try {
            const res = await fetchWithAuth("/cabinet/personal-info");
            if (!res.ok) throw new Error("Failed to load user data");

            const info = await res.json();

            document.getElementById("displayName").textContent = info.displayName || "—";
            document.getElementById("email").textContent = info.email || "—";
            document.getElementById("registerDate").textContent = new Date(info.registerDate).toLocaleDateString();

            const isSeller = info.isSeller === true;
            document.getElementById("isSeller").textContent = isSeller ? "seller" : "user";

            const sellerNicknameContainer = document.getElementById("sellerNicknameContainer");

            if (isSeller) {
                document.getElementById("sellerNickname").textContent = info.sellerNickname || "—";
                sellerNicknameContainer.classList.remove("hidden");
            } else {
                sellerNicknameContainer.classList.add("hidden");
            }

            userCard.classList.add("opacity-0", "translate-y-4");
            mainContent.classList.remove("hidden");
            setTimeout(() => {
                userCard.classList.remove("opacity-0", "translate-y-4");
                userCard.classList.add("opacity-100", "translate-y-0", "transition-all", "duration-500");
            });

        } catch (err) {
            console.error(err);
            userCard.innerHTML = `
                <p class="text-red-600 text-center">⚠ ${err.message}</p>
            `;
        }
    }

    await loadUserInfo();
});
