INSERT INTO role(id, name)
VALUES (1,'USER'),
       (2,'ADMIN'),
       (3,'SELLER');

INSERT INTO public."user"(id, email, password, display_name)
VALUES
    (1,'admin@gmail.com', '$2a$10$/UEXF5YfBUdODDaYxjx1feHmTdsnjD9GVTnmo.9RvHAxUNUA9No8W',
     'admin'),
    (0, 'service@system.local', '', 'marketplace');


INSERT INTO user_role(user_id, role_id)
VALUES
    (1, 1),
    (1, 2);
