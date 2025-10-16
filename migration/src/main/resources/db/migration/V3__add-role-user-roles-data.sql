INSERT INTO role(id, name)
VALUES (1,'USER'),
       (2,'ADMIN'),
       (3,'SELLER');

SELECT SETVAL('sequence_role', max(id)) FROM role;


INSERT INTO user_role(user_id, role_id)
VALUES
    (1, 1),
    (1, 2);