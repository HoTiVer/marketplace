import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const chatContainer = document.getElementById("chatContainer");
    const loading = document.getElementById("loading");
    const messagesEl = document.getElementById("messages");
    const chatTitle = document.getElementById("chatTitle");
    const messageInput = document.getElementById("messageInput");
    const sendBtn = document.getElementById("sendBtn");
    const sellerPageBtn = document.getElementById("sellerPageBtn");

    const pathParts = window.location.pathname.split("/").filter(Boolean);
    const chatId = Number(pathParts[pathParts.length - 1]);

    if (!chatId) {
        loading.textContent = " Chat ID not provided";
        return;
    }

    async function loadChat() {
        try {
            const response = await fetchWithAuth(`/cabinet/message/${chatId}`);
            if (!response.ok) {
                loading.textContent = `Failed to load chat (${response.status})`;
                return;
            }

            const chat = await response.json();
            loading.classList.add("hidden");
            chatContainer.classList.remove("hidden");

            chatTitle.textContent = chat.chatName;

            if (chat.isSeller && chat.chatName) {
                sellerPageBtn.classList.remove("hidden");
                sellerPageBtn.onclick = () => {
                    window.location.href = `/seller/${chat.chatName}`;
                };
            } else {
                sellerPageBtn.classList.add("hidden");
            }

            messagesEl.innerHTML = "";
            chat.messages.forEach(msg => {
                const msgDiv = document.createElement("div");

                const isMine = msg.senderId === userData.id;
                msgDiv.className = `
                    ${isMine ? "self-end bg-blue-100 text-blue-900" : "self-start bg-gray-200 text-gray-900"}
                    px-4 py-2 rounded-xl max-w-[70%]
                `;

                msgDiv.innerHTML = `
                    <p class="font-medium">${msg.senderName}</p>
                    <p>${msg.content}</p>
                    <p class="text-xs text-gray-500 mt-1">${new Date(msg.sentAt).toLocaleString()}</p>
                `;

                messagesEl.appendChild(msgDiv);
            });

            messagesEl.scrollTop = messagesEl.scrollHeight;

        } catch (err) {
            loading.textContent = `Error: ${err.message}`;
        }
    }

    sendBtn.onclick = async () => {
        const content = messageInput.value.trim();
        if (!content) return;

        try {
            const response = await fetchWithAuth(`/cabinet/message/${chatId}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ content })
            });

            if (!response.ok) {
                alert(` Failed to send message (${response.status})`);
                return;
            }

            messageInput.value = "";
            await loadChat();

        } catch (err) {
            alert(`Error sending message: ${err.message}`);
        }
    };
    messageInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendBtn.click();
        }
    });

    await loadChat();
});
