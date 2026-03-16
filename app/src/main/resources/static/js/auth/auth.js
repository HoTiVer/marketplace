export async function refreshAccessToken() {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) return null;

    try {
        const res = await fetch("/api/v1/auth/refresh", {
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

    if (token) {
        options.headers["Authorization"] = "Bearer " + token;
    }

    let res = await fetch(url, options);

    if (res.status === 401 && token) {
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

    await fetch("/api/v1/auth/logout",{
        method: "POST",
        headers: {"Authorization": "Bearer " + token }
    });
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/index.html";
}

export async function getAuthData(){
    try {
        let res = await fetchWithAuth("/api/v1/auth/me");

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
        const response = await fetch("/api/v1/auth/login", {
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

export async function registerUser(email, displayName, password) {
    try {
        const response = await fetch("/api/v1/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, displayName, password})
        });

        const data = await response.json();

        if (response.ok){
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