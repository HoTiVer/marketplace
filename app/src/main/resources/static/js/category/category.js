import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const container = document.getElementById("categoriesContainer");
    const adminPanel = document.getElementById("adminPanel");
    const modal = document.getElementById("modal");
    const categoryNameInput = document.getElementById("categoryName");
    const addCategoryBtn = document.getElementById("addCategoryBtn");
    const cancelBtn = document.getElementById("cancelBtn");
    const saveBtn = document.getElementById("saveBtn");
    const mainContent = document.getElementById("mainContent");

    //const isAdmin = userData?.roles?.some(r => r.name === "ADMIN") || false;
    const isAdmin = userData.roles?.includes("ADMIN") ?? false;
    if (isAdmin) adminPanel.classList.remove("hidden");

    let editingCategoryId = null;

    async function loadCategories() {
        try {
            const res = await fetch("/api/category");
            if (!res.ok) throw new Error("Failed to load categories");

            const categories = await res.json();
            container.innerHTML = "";

            if (categories.length === 0) {
                container.innerHTML = `<p class="text-gray-600">No categories yet.</p>`;
            } else {
                categories.forEach(cat => {
                    const card = document.createElement("div");
                    card.className = "bg-white rounded-2xl shadow-lg p-5 hover:shadow-xl transition flex flex-col justify-between";
                    card.innerHTML = `
                        <h2 class="text-xl font-semibold text-gray-800">${cat.name}</h2>
                        <p class="text-gray-500 text-sm">ID: ${cat.id}</p>
                    `;

                    if (isAdmin) {
                        const controls = document.createElement("div");
                        controls.className = "flex gap-2 mt-3";

                        const editBtn = document.createElement("button");
                        editBtn.className = "bg-yellow-400 px-3 py-1 rounded-lg hover:bg-yellow-500";
                        editBtn.textContent = "✏ Edit";
                        editBtn.onclick = () => {
                            editingCategoryId = cat.id;
                            categoryNameInput.value = cat.name;
                            modal.querySelector("h2").textContent = "Edit Category";
                            modal.classList.remove("hidden");
                        };

                        const delBtn = document.createElement("button");
                        delBtn.className = "bg-red-500 text-white px-3 py-1 rounded-lg hover:bg-red-600";
                        delBtn.textContent = "🗑 Delete";
                        delBtn.onclick = async () => {
                            if (confirm(`Delete category "${cat.name}"?`)) {
                                const delRes = await fetchWithAuth(`/api/category/${cat.id}`, { method: "DELETE" });
                                if (delRes.ok) loadCategories();
                                else alert("Failed to delete");
                            }
                        };

                        controls.appendChild(editBtn);
                        controls.appendChild(delBtn);
                        card.appendChild(controls);
                    }

                    container.appendChild(card);
                });
            }
        } catch (err) {
            container.innerHTML = `<p class="text-red-500">⚠ ${err.message}</p>`;
        }
    }

    mainContent.classList.remove("hidden");
    await loadCategories();

    addCategoryBtn.addEventListener("click", () => {
        editingCategoryId = null;
        categoryNameInput.value = "";
        modal.querySelector("h2").textContent = "Add Category";
        modal.classList.remove("hidden");
    });

    cancelBtn.addEventListener("click", () => {
        modal.classList.add("hidden");
        categoryNameInput.value = "";
    });

    saveBtn.addEventListener("click", async () => {
        const name = categoryNameInput.value.trim();
        if (!name) { alert("Category name is required"); return; }

        const method = editingCategoryId ? "PUT" : "POST";
        const url = editingCategoryId
            ? `/api/category/${editingCategoryId}`
            : "/api/category";

        const res = await fetchWithAuth(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: name })
        });

        if (res.ok) {
            modal.classList.add("hidden");
            categoryNameInput.value = "";
            await loadCategories();
        } else alert("Failed to save category");
    });
});
