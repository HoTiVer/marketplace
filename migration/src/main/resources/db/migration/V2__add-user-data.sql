INSERT INTO public."user"(id, email, password, display_name)
VALUES
    (1,'admin@gmail.com', '$2a$10$/UEXF5YfBUdODDaYxjx1feHmTdsnjD9GVTnmo.9RvHAxUNUA9No8W',
     'admin'),
    (0, 'service@system.local', '', 'marketplace');


SELECT SETVAL('sequence_user', max(id)) FROM public."user";