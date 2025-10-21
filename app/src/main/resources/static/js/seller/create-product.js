import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const userData = await getAuthData();
    await renderNavbar(userData);

    const form = document.getElementById("productForm");
    const container = document.getElementById("characteristicsContainer");
    const addBtn = document.getElementById("addCharacteristic");
    const categorySelect = document.getElementById("categorySelect");

    async function loadCategories() {
        try {
            const res = await fetchWithAuth("/api/category");
            if (!res.ok) throw new Error("Failed to load categories");

            const categories = await res.json();
            categorySelect.innerHTML = ""; // очищаем список

            if (categories.length === 0) {
                categorySelect.innerHTML = `<option value="">No categories available</option>`;
            } else {
                categorySelect.innerHTML = `<option value="">Select category...</option>`;
                categories.forEach(cat => {
                    const option = document.createElement("option");
                    option.value = cat.name; // или cat.id, если сервер ждёт id
                    option.textContent = cat.name;
                    categorySelect.appendChild(option);
                });
            }
        } catch (err) {
            categorySelect.innerHTML = `<option value="">⚠ ${err.message}</option>`;
        }
    }

    await loadCategories();

    addBtn.addEventListener("click", () => {
        const wrapper = document.createElement("div");
        wrapper.className = "flex gap-2 items-center";

        wrapper.innerHTML = `
      <input type="text" placeholder="Key" class="flex-1 border px-3 py-2 rounded-lg" required>
      <input type="text" placeholder="Value" class="flex-1 border px-3 py-2 rounded-lg" required>
      <button type="button" class="removeBtn text-red-500 hover:text-red-700">✖</button>
    `;

        wrapper.querySelector(".removeBtn").addEventListener("click", () => wrapper.remove());
        container.appendChild(wrapper);
    });

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const characteristic = {};
        container.querySelectorAll("div").forEach(div => {
            const inputs = div.querySelectorAll("input");
            const key = inputs[0].value.trim();
            const value = inputs[1].value.trim();
            if (key && value) characteristic[key] = value;
        });

        const data = {
            name: document.getElementById("name").value.trim(),
            price: parseFloat(document.getElementById("price").value),
            description: document.getElementById("description").value.trim(),
            categoryName: categorySelect.value,
            characteristic
        };

        try {
            const res = await fetchWithAuth("/product", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });

            if (res.ok) {
                alert("Product created successfully!");
                form.reset();
                container.innerHTML = "";
            } else {
                const msg = await res.text();
                alert("Error creating product: " + msg);
            }
        } catch (err) {
            alert("Network error: " + err.message);
        }
    });
});
