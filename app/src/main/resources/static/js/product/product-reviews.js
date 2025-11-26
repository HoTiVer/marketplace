import { fetchWithAuth, getAuthData } from "../auth/auth.js";
import { renderNavbar } from "../navbar.js";

document.addEventListener("DOMContentLoaded", async () => {
    const user = await getAuthData();
    await renderNavbar(user);

    const path = window.location.pathname.split("/").filter(Boolean);
    const productId = Number(path[path.length - 2]);

    const productNameEl = document.getElementById("productName");
    const reviewsContainer = document.getElementById("reviewsContainer");
    const ratingSelect = document.getElementById("rating");
    const commentInput = document.getElementById("comment");
    const submitBtn = document.getElementById("submitReviewBtn");
    const messageEl = document.getElementById("reviewMessage");

    const LOAD_COUNT = 5;
    let shownReviews = 0;
    let allReviews = [];

    function renderReviews() {
        reviewsContainer.innerHTML = "";

        const sliceEnd = Math.min(shownReviews + LOAD_COUNT, allReviews.length);
        const toShow = allReviews.slice(0, sliceEnd);

        toShow.forEach(r => {
            const stars = "⭐".repeat(r.rating);
            const card = `
                <div class="bg-white p-4 rounded-lg shadow">
                    <p class="font-semibold text-gray-800">${r.commentatorName}</p>
                    <p class="text-yellow-500">${stars}</p>
                    <p class="text-gray-700 mt-1">${r.comment}</p>
                    <p class="text-gray-400 text-sm mt-1">
                        ${new Date(r.updatedAt)
                            .toLocaleDateString(undefined, { 
                                year: 'numeric',
                                month: 'short',
                                day: 'numeric' }
                            )}
                    </p>
                </div>
            `;
            reviewsContainer.innerHTML += card;
        });

        shownReviews = sliceEnd;
        toggleLoadMoreBtn();
    }

    function toggleLoadMoreBtn() {
        const btn = document.getElementById("loadMoreBtn");

        if (!btn) return;
        if (shownReviews >= allReviews.length) {
            btn.classList.add("hidden");
        } else {
            btn.classList.remove("hidden");
        }
    }

    async function loadProductReviews() {
        const res = await fetchWithAuth(`/api/product/${productId}/review`);
        const product = await res.json();

        productNameEl.textContent = product.productName;
        allReviews = product.productReviews ?? [];
        shownReviews = 0;

        if (allReviews.length === 0) {
            reviewsContainer.innerHTML = `<p class="text-gray-500 italic">No reviews yet</p>`;
            return;
        }

        renderReviews();
    }

    document.getElementById("loadMoreBtn").onclick = () => {
        renderReviews();
    };

    productNameEl.onclick = () => {
        window.location.href = `/product/${productId}`;
    };

    submitBtn.onclick = async () => {
        const review = {
            rating: Number(ratingSelect.value),
            comment: commentInput.value
        };

        const res = await fetchWithAuth(`/api/product/${productId}/review`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(review)
        });

        const data = await res.json();
        messageEl.textContent = data.message;
        messageEl.classList.remove("hidden");

        if (res.ok) {
            commentInput.value = "";
            await loadProductReviews();
        } else {
            messageEl.classList.add("text-red-600");
        }
    };

    await loadProductReviews();
});
