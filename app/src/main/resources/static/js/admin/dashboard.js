import { getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const authData = await getAuthData();
    await renderNavbar(authData);

    const hasAdminRole = authData?.roles?.includes("ADMIN") ?? false;
    if (!hasAdminRole) {
        alert("Access denied. Admins only.");
        window.location.href = "/index.html";
    }
});
