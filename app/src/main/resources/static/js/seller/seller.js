import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const sellerContainer = document.getElementById("sellerContainer");
    const loading = document.getElementById("loading");
    const viewProductsBtn = document.getElementById("viewProductsBtn");

    const sendMessageBtn = document.getElementById("sendMessageBtn");
    const messageContent = document.getElementById("messageContent");
    const messageStatus = document.getElementById("messageStatus");

    const pathParts = window.location.pathname.split("/").filter(Boolean);
    const sellerNickname = pathParts[pathParts.length - 1];

    if (!sellerNickname) {
        loading.textContent = "Seller nickname not provided";
        return;
    }

    try {
        const response = await fetchWithAuth(`/api/seller/${sellerNickname}`);

        if (!response.ok) {
            loading.textContent = `Failed to load seller (${response.status})`;
            return;
        }

        const seller = await response.json();
        loading.classList.add("hidden");
        sellerContainer.classList.remove("hidden");

        document.getElementById("displayName").textContent = seller.displayName || "—";
        document.getElementById("nickname").textContent = seller.nickname || "—";
        document.getElementById("rating").textContent = seller.rating?.toFixed(2) ?? "0.00";
        document.getElementById("profileDescription").textContent =
            seller.profileDescription || "No description provided.";

        viewProductsBtn.href = `/seller/${sellerNickname}/products`;


        sendMessageBtn.addEventListener("click", async () => {
            const content = messageContent.value.trim();

            if (!content) {
                messageStatus.textContent = "Please write a message first.";
                messageStatus.classList.remove("hidden", "text-green-600");
                messageStatus.classList.add("text-red-600");
                return;
            }

            try {
                const res = await fetchWithAuth(`/api/seller/message/${sellerNickname}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ content })
                });

                if (!res.ok) {
                    messageStatus.textContent = `Failed to send message (${res.status})`;
                    messageStatus.classList.remove("hidden", "text-green-600");
                    messageStatus.classList.add("text-red-600");
                    return;
                }

                messageContent.value = "";
                messageStatus.textContent = "Message sent successfully!";
                messageStatus.classList.remove("hidden", "text-red-600");
                messageStatus.classList.add("text-green-600");

            } catch (err) {
                messageStatus.textContent = `Error: ${err.message}`;
                messageStatus.classList.remove("hidden", "text-green-600");
                messageStatus.classList.add("text-red-600");
            }
        });

    } catch (err) {
        loading.textContent = `Error: ${err.message}`;
    }
});
