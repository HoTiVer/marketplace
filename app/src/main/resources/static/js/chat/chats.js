import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const loading = document.getElementById("loading");
    const chatsContainer = document.getElementById("chatsContainer");

    try {
        const res = await fetchWithAuth("/cabinet/message");

        if (!res.ok) {
            loading.textContent = `❌ Failed to load chats (${res.status})`;
            return;
        }

        const chats = await res.json();

        if (!chats.length) {
            loading.textContent = "You have no chats yet.";
            return;
        }

        loading.classList.add("hidden");
        chatsContainer.classList.remove("hidden");

        chats.forEach(chat => {
            const div = document.createElement("div");
            div.className = "bg-white rounded-lg shadow p-4 cursor-pointer hover:bg-gray-50 transition";
            div.textContent = chat.name;
            div.onclick = () => {
                window.location.href = `/cabinet/message/${chat.chatId}`;
            };
            chatsContainer.appendChild(div);
        });

    } catch (err) {
        loading.textContent = `⚠️ Error: ${err.message}`;
    }
});
