INSERT INTO category
VALUES
    (1, 'electronics'),
    (2, 'clothing'),
    (3, 'home'),
    (4, 'kitchen'),
    (5, 'sports'),
    (6, 'books'),
    (7, 'food'),
    (8, 'cars'),
    (9, 'games'),
    (10, 'toys'),
    (11, 'software'),
    (12, 'construction tools');

SELECT SETVAL('sequence_category', max(id)) FROM category;
