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
            window.location.href = `/user/profile/${authData.nickname}`;
        });

        const hasAdminRole = authData.roles?.includes("ADMIN") ?? false;

        if (hasAdminRole) {
            const categoryBtn = document.createElement("a");
            categoryBtn.href = "/category.html";
            categoryBtn.textContent = "Category";
            categoryBtn.className = "w-full block px-4 py-2 text-left text-yellow-600 hover:bg-yellow-50 transition";

            dropdown.insertBefore(categoryBtn, logoutBtn);
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