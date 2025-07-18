CREATE TABLE sellers(
    id BIGINT PRIMARY KEY REFERENCES users(id),
    nickname VARCHAR(20) NOT NULL UNIQUE,
    rating NUMERIC(2,1) NOT NULL,
    profile_description VARCHAR(400)
);

ALTER TABLE users ADD COLUMN register_date DATE NOT NULL DEFAULT CURRENT_DATE;

CREATE TABLE register_seller_request(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    requested_nickname VARCHAR(20),
    display_name VARCHAR(40),
    profile_description VARCHAR(400)
)