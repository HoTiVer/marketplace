INSERT INTO seller(id, nickname, rating, profile_description)
VALUES (2, 'mike-seller', 4, 'A sell different stuff'),
       (5, 'bestLibrary', 3, 'best books'),
       (6, 'pc-shop', 5, 'best pc parts'),
       (7, 'toy-world', 4, 'best toys'),
       (8, 'gym-shop', 2, 'best gym equipment'),
       (9, 'japan-food', 5, 'best food from Japan');


INSERT INTO user_role(user_id, role_id)
VALUES (2, 3),
       (5, 3),
       (6, 3),
       (7, 3),
       (8, 3),
       (9, 3);
