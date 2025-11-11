CREATE SEQUENCE IF NOT EXISTS public.sequence_user START WITH 1 INCREMENT BY 5;

CREATE SEQUENCE IF NOT EXISTS sequence_role START WITH 1 INCREMENT BY 5;

CREATE SEQUENCE IF NOT EXISTS sequence_category START WITH 1 INCREMENT BY 5;

CREATE SEQUENCE IF NOT EXISTS sequence_product START WITH 1 INCREMENT BY 5;

CREATE SEQUENCE IF NOT EXISTS sequence_register_seller_request START WITH 1 INCREMENT BY 5;

CREATE SEQUENCE IF NOT EXISTS sequence_chat START WITH 1 INCREMENT BY 5;

CREATE SEQUENCE IF NOT EXISTS sequence_message START WITH 1 INCREMENT BY 5;

--CREATE SEQUENCE IF NOT EXISTS sequence_user_wishes START WITH 1 INCREMENT BY 5;

CREATE TABLE public."user"(
    id BIGINT,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(270) NOT NULL,
    display_name VARCHAR(40) NOT NULL,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0,
    register_date DATE NOT NULL DEFAULT CURRENT_DATE,
    is_two_factor_enable BOOLEAN DEFAULT FALSE,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE role(
    id BIGINT,
    name VARCHAR(25) NOT NULL,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE user_role(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role_id)
);

CREATE TABLE category(
    id BIGINT,
    name VARCHAR(50),
    CONSTRAINT pk_category PRIMARY KEY (id)
);

CREATE TABLE product(
    id BIGINT,
    name VARCHAR(100),
    price DOUBLE PRECISION,
    description VARCHAR(500),
    category_id BIGINT NOT NULL,
    characteristic JSONB,
    seller_id BIGINT NOT NULL,
    is_visible BOOLEAN NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (id)
);

CREATE TABLE seller(
    id BIGINT,
    nickname VARCHAR(20) NOT NULL,
    rating NUMERIC(2,1) NOT NULL CHECK ( rating BETWEEN 1 AND 5),
    profile_description VARCHAR(400) NOT NULL,
    CONSTRAINT pk_seller PRIMARY KEY (id)
);

CREATE TABLE register_seller_request(
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    request_date DATE NOT NULL DEFAULT NOW(),
    requested_nickname VARCHAR(20) NOT NULL,
    display_name VARCHAR(40) NOT NULL,
    profile_description VARCHAR(400) NOT NULL,
    status VARCHAR(25) NOT NULL,
    CONSTRAINT pk_register_seller_request PRIMARY KEY (id)
);

CREATE TABLE chat(
    id BIGINT,
    user1_id BIGINT,
    user2_id BIGINT,
    CONSTRAINT pk_chat PRIMARY KEY (id)
);

CREATE TABLE message(
    id BIGINT,
    chat_id BIGINT,
    sender_id BIGINT,
    content VARCHAR(500),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_message PRIMARY KEY (id)
);

CREATE TABLE user_wishes(
    user_id BIGINT,
    product_id BIGINT,
    CONSTRAINT pk_user_wishes PRIMARY KEY (user_id, product_id)
);

ALTER TABLE public."user"
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE role
    ADD CONSTRAINT uc_role_name UNIQUE (name);

ALTER TABLE seller
    ADD CONSTRAINT uc_seller_nickname UNIQUE (nickname);

ALTER TABLE category
    ADD CONSTRAINT uc_category_name UNIQUE (name);



ALTER TABLE user_role
    ADD CONSTRAINT fk_user_role_on_user FOREIGN KEY (user_id) REFERENCES public."user"(id);

ALTER TABLE user_role
    ADD CONSTRAINT fk_user_role_on_role FOREIGN KEY (role_id) REFERENCES role(id);

ALTER TABLE product
    ADD CONSTRAINT fk_product_on_category FOREIGN KEY (category_id) REFERENCES category(id);

ALTER TABLE product
    ADD CONSTRAINT fk_product_on_seller FOREIGN KEY (seller_id) REFERENCES seller(id);

ALTER TABLE seller
    ADD CONSTRAINT fk_seller_on_user FOREIGN KEY (id) REFERENCES public."user"(id);

ALTER TABLE register_seller_request
    ADD CONSTRAINT fk_register_seller_request_on_user FOREIGN KEY (user_id) REFERENCES public."user"(id);

ALTER TABLE chat
    ADD CONSTRAINT fk_chat_on_user1 FOREIGN KEY (user1_id) REFERENCES public."user"(id);

ALTER TABLE chat
    ADD CONSTRAINT fk_chat_on_user2 FOREIGN KEY (user2_id) REFERENCES public."user"(id);

ALTER TABLE message
    ADD CONSTRAINT fk_message_on_chat FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE;

ALTER TABLE message
    ADD CONSTRAINT fk_message_on_user FOREIGN KEY (sender_id) REFERENCES public."user"(id);

ALTER TABLE user_wishes
    ADD CONSTRAINT fk_user_wishes_on_user FOREIGN KEY (user_id) REFERENCES public."user"(id);

ALTER TABLE user_wishes
    ADD CONSTRAINT fk_user_wishes_on_product FOREIGN KEY (product_id) REFERENCES product(id);

