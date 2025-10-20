import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const authData = await getAuthData();
    await renderNavbar(authData);

    const hasAdminRole = authData?.roles?.includes("ADMIN") ?? false;
    if (!hasAdminRole) {
        alert("Access denied. Admins only.");
        window.location.href = "/index.html";
        return;
    }

    const container = document.getElementById("requestsContainer");

    async function loadRequests() {
        try {
            const res = await fetchWithAuth("/api/admin/request/seller-register");
            if (!res.ok) throw new Error("Failed to load seller requests");

            const requests = await res.json();
            container.innerHTML = "";

            if (requests.length === 0) {
                container.innerHTML = `<p class="text-gray-600 text-center">No pending seller requests</p>`;
            } else {
                requests.forEach(req => {
                    const card = document.createElement("div");
                    card.className = "bg-white shadow-md rounded-xl p-4 flex flex-col gap-2";
                    card.innerHTML = `
                        <p><strong>ID:</strong> ${req.id}</p>
                        <p><strong>User ID:</strong> ${req.userId}</p>
                        <p><strong>Requested Nickname:</strong> ${req.requestedNickname}</p>
                        <p><strong>Display Name:</strong> ${req.displayName}</p>
                        <p><strong>Description:</strong> ${req.profileDescription}</p>
                        <div class="flex gap-2 mt-2">
                            <button class="acceptBtn bg-green-500 text-white px-3 py-1 rounded hover:bg-green-600">Accept</button>
                            <button class="deleteBtn bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600">Delete</button>
                        </div>
                    `;

                    const acceptBtn = card.querySelector(".acceptBtn");
                    const deleteBtn = card.querySelector(".deleteBtn");

                    acceptBtn.addEventListener("click", async () => {
                        try {
                            const postRes = await fetchWithAuth(`/api/admin/request/seller-register/${req.id}`, {
                                method: "POST"
                            });
                            if (postRes.ok) {
                                alert("Request accepted ✅");
                                await loadRequests();
                            } else {
                                const text = await postRes.text();
                                alert("Failed to accept: " + text);
                            }
                        } catch (err) {
                            alert("Error: " + err.message);
                        }
                    });

                    deleteBtn.addEventListener("click", async () => {
                        if (!confirm("Are you sure you want to delete this request?")) return;
                        try {
                            const delRes = await fetchWithAuth(`/api/admin/request/seller-register${req.id}`, {
                                method: "DELETE"
                            });
                            if (delRes.ok) {
                                alert("Request deleted ✅");
                                await loadRequests();
                            } else {
                                const text = await delRes.text();
                                alert("Failed to delete: " + text);
                            }
                        } catch (err) {
                            alert("Error: " + err.message);
                        }
                    });

                    container.appendChild(card);
                });
            }

        } catch (err) {
            container.innerHTML = `<p class="text-red-600 text-center">⚠ ${err.message}</p>`;
        }
    }

    await loadRequests();
});
