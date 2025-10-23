import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const mainContent = document.getElementById("mainContent");
    const userCard = document.getElementById("userCard");
    const becomeSellerBtn = document.getElementById("becomeSellerBtn");
    const goToSellerPageBtn = document.getElementById("goToSellerPageBtn");
    const sellerModal = document.getElementById("sellerModal");
    const cancelSellerBtn = document.getElementById("cancelSellerBtn");
    const submitSellerBtn = document.getElementById("submitSellerBtn");

    async function loadUserInfo() {
        try {
            const res = await fetchWithAuth("/cabinet/personal-info");
            if (!res.ok) throw new Error("Failed to load user data");

            const info = await res.json();

            document.getElementById("displayName").textContent = info.displayName || "—";
            document.getElementById("email").textContent = info.email || "—";
            document.getElementById("registerDate").textContent = new Date(info.registerDate).toLocaleDateString();

            const isSeller = info.isSeller === true;
            document.getElementById("isSeller").textContent = isSeller ? "seller" : "not seller";

            const sellerNicknameContainer = document.getElementById("sellerNicknameContainer");

            if (isSeller) {
                const nickname = info.sellerNickname || "—";
                document.getElementById("sellerNickname").textContent = nickname;
                sellerNicknameContainer.classList.remove("hidden");
                becomeSellerBtn.classList.add("hidden");

                // Показываем кнопку перехода
                goToSellerPageBtn.classList.remove("hidden");
                goToSellerPageBtn.onclick = () => {
                    window.location.href = `/seller/${nickname}`;
                };
            } else {
                sellerNicknameContainer.classList.add("hidden");
                becomeSellerBtn.classList.remove("hidden");
                goToSellerPageBtn.classList.add("hidden");
            }

            mainContent.classList.remove("hidden");

        } catch (err) {
            console.error(err);
            userCard.innerHTML = `<p class="text-red-600 text-center">⚠ ${err.message}</p>`;
        }
    }

    // --- Become Seller Button ---
    becomeSellerBtn.addEventListener("click", () => {
        sellerModal.classList.remove("hidden");
    });

    cancelSellerBtn.addEventListener("click", () => {
        sellerModal.classList.add("hidden");
    });

    submitSellerBtn.addEventListener("click", async () => {
        const nickname = document.getElementById("requestedNickname").value.trim();
        const displayNameInput = document.getElementById("displayNameInput").value.trim();
        const description = document.getElementById("description").value.trim();

        if (!nickname || !displayNameInput || !description) {
            alert("Please fill in all fields");
            return;
        }

        const body = {
            requestedNickname: nickname,
            displayName: displayNameInput,
            description: description
        };

        try {
            const res = await fetchWithAuth("/cabinet/new-seller/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            const result = await res.json();

            alert(result.message || "Unknown server response");
            sellerModal.classList.add("hidden");

            if (res.ok) {
                await loadUserInfo();
            }

        } catch (err) {
            alert("Failed to send request: " + err.message);
        }
    });

    await loadUserInfo();
});
