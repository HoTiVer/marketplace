import { logout } from "./auth/auth.js";

export async function renderNavbar(authData) {
    const navbarContainer = document.getElementById("navbar");
    if (!navbarContainer) return;

    navbarContainer.style.visibility = "hidden";

    const html = await fetch("/components/navbar.html").then(r => r.text());
    navbarContainer.innerHTML = html;

    const authButtons = document.getElementById("authButtons");
    const userMenu = document.getElementById("userMenu");
    const menuBtn = document.getElementById("menuBtn");
    const dropdown = document.getElementById("dropdown");
    const logoutBtn = document.getElementById("logoutBtn");
    const profileBtn = document.getElementById("profileBtn");

    if (authData) {
        authButtons.classList.add("hidden");
        userMenu.classList.remove("hidden");

        profileBtn.addEventListener("click", () => {
            window.location.href = `/user/personal-info.html`;
        });

        const hasAdminRole = authData.roles?.includes("ADMIN") ?? false;
        const hasSellerRole = authData.roles?.includes("SELLER") ?? false;

        if (hasAdminRole) {
            const adminBtn = document.createElement("a");
            adminBtn.href = "/admin/dashboard.html";
            adminBtn.textContent = "Admin Panel";
            adminBtn.className =
                "w-full block px-4 py-2 text-left text-yellow-600 hover:bg-yellow-50 transition";
            dropdown.insertBefore(adminBtn, logoutBtn);

            const categoryBtn = document.createElement("a");
            categoryBtn.href = "/category.html";
            categoryBtn.textContent = "Categories";
            categoryBtn.className =
                "w-full block px-4 py-2 text-left text-blue-600 hover:bg-blue-50 transition";
            dropdown.insertBefore(categoryBtn, logoutBtn);
        }

        if (hasSellerRole) {
            const sellerBtn = document.createElement("a");
            sellerBtn.href = "/seller/create-product.html";
            sellerBtn.textContent = "Create Product";
            sellerBtn.className =
                "w-full block px-4 py-2 text-left text-green-600 hover:bg-green-50 transition";
            dropdown.insertBefore(sellerBtn, logoutBtn);
        }

    } else {
        authButtons.classList.remove("hidden");
        userMenu.classList.add("hidden");
    }

    menuBtn.addEventListener("click", () => dropdown.classList.toggle("hidden"));
    logoutBtn.addEventListener("click", logout);

    document.addEventListener("click", (e) => {
        if (!userMenu.contains(e.target)) dropdown.classList.add("hidden");
    });

    navbarContainer.style.visibility = "visible";
}
