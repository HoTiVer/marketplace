CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(40) UNIQUE NOT NULL,
    password VARCHAR(200) NOT NULL,
    display_username VARCHAR(20) UNIQUE,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0
);

CREATE TABLE roles(
    id BIGSERIAL PRIMARY KEY,
    role_type VARCHAR(25)
);

CREATE TABLE users_roles(
    user_id BIGINT,
    role_id BIGINT
);

INSERT INTO roles(role_type)
VALUES ('USER'), ('ADMIN'), ('SELLER');

CREATE TABLE products(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    price DOUBLE PRECISION,
    description VARCHAR(500)
);