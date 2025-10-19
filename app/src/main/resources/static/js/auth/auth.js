// auth.js
export async function refreshAccessToken() {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) return null;

    try {
        const res = await fetch("/auth/refresh", {
            method: "POST",
            headers: { "Authorization": "Bearer " + refreshToken }
        });

        if (res.ok) {
            const data = await res.json();
            localStorage.setItem("accessToken", data.accessToken);
            return data.accessToken;
        } else {
            logout();
            return null;
        }
    } catch (err) {
        console.error("Failed to refresh token:", err);
        logout();
        return null;
    }
}

export async function fetchWithAuth(url, options = {}) {
    let token = localStorage.getItem("accessToken");
    if (!options.headers) options.headers = {};
    options.headers["Authorization"] = "Bearer " + token;

    let res = await fetch(url, options);

    if (res.status === 401) {
        token = await refreshAccessToken();
        if (token) {
            options.headers["Authorization"] = "Bearer " + token;
            res = await fetch(url, options);
        }
    }
    return res;
}

export async function logout() {
    const token = localStorage.getItem("accessToken");

    await fetch("/auth/logout",{
        method: "POST",
        headers: {"Authorization": "Bearer " + token }
    });
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/index.html";
}

// export async function isAuthenticated() {
//     const token = localStorage.getItem("accessToken")
//     if (!token) return false;
//
//     try {
//         const res = await fetch("/auth/validate-access", {
//             method: "POST",
//             headers: {"Authorization": "Bearer " + token}
//         })
//         if (res.status === 401)
//             return {authenticated: false, reason: "unauthorized"}
//
//         if (res.status === 403)
//             return { authenticated: false, reason: "forbidden"}
//
//         return { authenticated: res.ok, reason: res.ok ? "ok" : "error" };
//     } catch (err) {
//         console.error("Auth validation failed:", err);
//         return { authenticated: false, reason: "network_error" };
//     }
// }

export async function getAuthData(){
    try {
        let res = await fetchWithAuth("/auth/me");

        if (res.status === 401) {
            return null;
        }

        if (res.ok) {
            return await res.json();
        } else {
            return null;
        }
    } catch (err) {
        console.error("Failed to get auth data:", err);
        return null;
    }
}

export async function loginUser(email, password) {
    try {
        const response = await fetch("/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (data.isSuccess) {
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);
        }
        return data;
    } catch (err) {
        console.error("Login failed:", err);
        return {
            isSuccess: false,
            message: "Network error",
            accessToken: null,
            refreshToken: null
        };
    }
}

export async function registerUser(email, nickname, password) {
    try {
        const response = await fetch("/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, nickname, password})
        });

        const data = await response.json();

        if (data.isSuccess){
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);
        }
        return data;

    } catch (err){
        console.error("Registration failed:", err)
        return {
            isSuccess: false,
            message: "Network error",
            accessToken: null,
            refreshToken: null
        };
    }
}