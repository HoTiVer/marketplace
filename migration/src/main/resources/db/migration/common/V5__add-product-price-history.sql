CREATE SEQUENCE sequence_product_price_history START WITH 1 INCREMENT BY 5;

CREATE SEQUENCE sequence_product_promotion START WITH 1 INCREMENT BY 5;

CREATE TABLE product_price_history (
    id BIGINT,
    product_id BIGINT NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_product_price_history PRIMARY KEY (id)
);

CREATE TABLE product_promotion(
    id BIGINT,
    product_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    discount_percent INT CHECK ( discount_percent BETWEEN 0 AND 100) DEFAULT 0,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    active BOOLEAN DEFAULT FALSE,
    show_end_date BOOLEAN DEFAULT FALSE,
    CONSTRAINT pk_product_promotion PRIMARY KEY (id)
);

ALTER TABLE product_price_history
    ADD CONSTRAINT fk_product_price_history_on_product FOREIGN KEY (product_id)
        REFERENCES product(id) ON DELETE CASCADE;

ALTER TABLE product_promotion
    ADD CONSTRAINT fk_product_percent_promotion_on_product FOREIGN KEY (product_id)
        REFERENCES product(id) ON DELETE CASCADE;
