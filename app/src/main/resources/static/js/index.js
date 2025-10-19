import { renderNavbar } from "./navbar.js";
import { getAuthData } from "./auth/auth.js";

document.addEventListener("DOMContentLoaded", async () =>{
    const data = await getAuthData();
    await renderNavbar(data);
});