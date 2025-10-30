import { registerUser } from "./auth.js";

document.addEventListener("DOMContentLoaded", () => {
    const registerForm = document.getElementById("registerForm");
    const msgEl = document.getElementById("responseMessage");

    registerForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const body = {
            email: registerForm.email.value,
            displayName: registerForm.displayName.value,
            password: registerForm.password.value
        };

        const data = await registerUser(body.email, body.displayName, body.password);

        if (data.isSuccess) {
            msgEl.textContent = data.message || "Registration successful!";
            msgEl.className = "text-green-600 text-sm mt-4 text-center";

            const redirectPath = "/";

            setTimeout(() => window.location.href = redirectPath, 800);
        } else {
            msgEl.textContent = data.message;
            msgEl.className = "text-red-600 text-sm mt-4 text-center";
        }
    });
});
