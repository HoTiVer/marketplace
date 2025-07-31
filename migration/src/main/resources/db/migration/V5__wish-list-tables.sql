CREATE TABLE users_wishes(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT
)