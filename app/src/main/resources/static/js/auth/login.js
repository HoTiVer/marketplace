import { loginUser } from "./auth.js";

document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    const msgEl = document.getElementById("responseMessage");

    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const body = {
            email: loginForm.email.value,
            password: loginForm.password.value
        };

        try {
            const data = await loginUser(body.email, body.password);

            if (data.isSuccess) {
                msgEl.textContent = "Login successful!";
                msgEl.className = "text-green-600 text-sm mt-4 text-center";

                const redirectPath = localStorage.getItem("afterLoginRedirect") || "/";
                localStorage.removeItem("afterLoginRedirect");

                setTimeout(() => window.location.href = redirectPath, 800);
            } else {
                msgEl.textContent = "Invalid email or password";
                msgEl.className = "text-red-600 text-sm mt-4 text-center";
            }
        } catch (err) {
            console.error(err);
            msgEl.textContent = "Network error";
            msgEl.className = "text-red-600 text-sm mt-4 text-center";
        }
    });
});
