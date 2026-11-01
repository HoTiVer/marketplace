INSERT INTO category
VALUES
    (1, 'food'),
    (2, 'electronics'),
    (3, 'clothing');

SELECT SETVAL('sequence_category', max(id)) FROM category;