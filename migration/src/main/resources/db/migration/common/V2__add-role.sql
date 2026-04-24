INSERT INTO role(id, name)
VALUES (1,'USER'),
       (2,'ADMIN'),
       (3,'SELLER');

SELECT SETVAL('sequence_role', max(id)) FROM role;