CREATE TABLE chats (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    user2_id BIGINT REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE message (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT REFERENCES chats(id) ON DELETE CASCADE,
    sender_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    content VARCHAR(500),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (id, email, password, balance, register_date, display_name)
VALUES (0, 'service@system.local', '', 0.0,
        CURRENT_DATE, 'marketplace');