CREATE TABLE product_image (
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    is_main BOOLEAN DEFAULT FALSE,
    product_id BIGINT NOT NULL,
    CONSTRAINT fk_product
        FOREIGN KEY (product_id)
            REFERENCES product(id)
            ON DELETE CASCADE
);
