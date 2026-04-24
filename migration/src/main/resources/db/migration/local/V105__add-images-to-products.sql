INSERT INTO product_image (id, url, is_main, product_id) VALUES
-- electronics
(1,  '/products/1/main.jpg',  true, 1),
(2,  '/products/2/main.jpg',  true, 2),
(3,  '/products/3/main.jpg',  true, 3),
(4,  '/products/4/main.jpeg',  true, 4),
(5,  '/products/5/main.jpg',  true, 5),

-- clothes
(6,  '/products/6/main.jpg',  true, 6),
(7,  '/products/7/main.jpg',  true, 7),
(8,  '/products/8/main.jpg',  true, 8),
(9,  '/products/9/main.jpg',  true, 9),
(10, '/products/10/main.jpg', true, 10),

-- home
(11, '/products/11/main.jpg', true, 11),
(12, '/products/12/main.jpg', true, 12),

-- kitchen
(13, '/products/13/main.jpg', true, 13),
(14, '/products/14/main.jpg', true, 14),
(15, '/products/15/main.jpg', true, 15),

-- sport
(16, '/products/16/main.jpg', true, 16),
(17, '/products/17/main.jpg', true, 17),
(18, '/products/18/main.jpg', true, 18),
(19, '/products/19/main.jpg', true, 19),
(20, '/products/20/main.jpg', true, 20),

-- books
(21, '/products/21/main.jpeg', true, 21),
(22, '/products/22/main.jpg', true, 22),
(23, '/products/23/main.jpg', true, 23),

-- food
(24, '/products/24/main.jpg', true, 24),
(25, '/products/25/main.jpg', true, 25),
(26, '/products/26/main.jpg', true, 26),
(27, '/products/27/main.jpg', true, 27),

-- auto
(28, '/products/28/main.jpg', true, 28),
(29, '/products/29/main.jpg', true, 29),

-- games
(30, '/products/30/main.jpg', true, 30);

-- sync sequence
SELECT SETVAL('sequence_product_image', (SELECT MAX(id) FROM product_image));
