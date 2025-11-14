INSERT INTO product(id, name, price, description, category_id, characteristic, seller_id, is_visible, stock_quantity, sales_count, publishing_date)
VALUES
-- seller 6
(1, 'Gaming Laptop', 1199.99, 'High performance gaming laptop', 1,
 '{"brand": "ASUS", "RAM": "16GB", "GPU": "RTX 3060", "storage": "1TB SSD"}', 6, true, 14, 6, '2025-01-12'),

(2, 'Mechanical Keyboard', 89.99, 'RGB mechanical keyboard with blue switches', 1,
 '{"brand": "Keychron", "switches": "blue", "layout": "ANSI"}', 6, true, 32, 18, '2025-02-03'),

(3, 'Wireless Mouse', 24.99, 'Ergonomic wireless mouse with silent clicks', 1,
 '{"brand": "Logitech", "battery": "AA", "dpi": "1600"}', 6, true, 47, 53, '2025-03-21'),

(4, '4K Monitor', 299.99, 'Ultra HD 4K monitor 27-inch', 1,
 '{"brand": "Samsung", "resolution": "3840x2160", "refreshRate": "75Hz"}', 6, true, 20, 10, '2025-03-01'),

(5, 'External SSD', 129.99, 'Fast external SSD 1TB', 1,
 '{"brand": "SanDisk", "speed": "1050MB/s", "capacity": "1TB"}', 6, true, 28, 22, '2025-01-25'),

-- seller 2
(6, 'Men Hoodie', 45.50, 'Warm cotton hoodie for men', 2,
 '{"color": "black", "size": "L", "material": "cotton"}', 2, true, 38, 12, '2025-04-18'),

(7, 'Women Jacket', 79.99, 'Lightweight jacket for spring', 2,
 '{"color": "beige", "size": "M", "material": "polyester"}', 2, true, 17, 7, '2025-03-10'),

(8, 'Sneakers', 99.00, 'Running sneakers with breathable mesh', 2,
 '{"brand": "Nike", "size": "42", "color": "white"}', 2, true, 21, 19, '2025-02-14'),

(9, 'Baseball Cap', 19.99, 'Adjustable unisex cap', 2,
 '{"color": "navy", "material": "cotton"}', 2, true, 52, 34, '2025-05-08'),

(10, 'Jeans', 59.90, 'Classic blue jeans', 2,
 '{"size": "32", "fit": "regular", "color": "blue"}', 2, true, 25, 15, '2025-06-01'),

-- seller 2
(11, 'LED Lamp', 35.00, 'Energy saving LED lamp with warm light', 3,
 '{"power": "15W", "colorTemp": "3000K"}', 2, true, 44, 40, '2025-02-28'),

(12, 'Wall Clock', 22.50, 'Minimalist wall clock', 3,
 '{"diameter": "30cm", "material": "wood"}', 2, true, 36, 19, '2025-07-12'),

-- seller 2
(13, 'Knife Set', 89.00, 'Set of 5 stainless steel knives', 4,
 '{"blades": "stainless steel", "pieces": 5, "handle": "wood"}', 2, true, 18, 12, '2025-08-03'),

(14, 'Blender', 49.99, '500W blender with glass jug', 4,
 '{"power": "500W", "capacity": "1.5L"}', 2, true, 26, 22, '2025-01-17'),

(15, 'Non-stick Pan', 34.99, 'Frying pan with non-stick coating', 4,
 '{"diameter": "28cm", "material": "aluminum"}', 2, true, 41, 33, '2025-09-05'),

-- seller 8
(16, 'Yoga Mat', 29.99, 'Non-slip yoga mat 6mm', 5,
 '{"color": "purple", "length": "180cm"}', 8, true, 48, 27, '2025-06-21'),

(17, 'Dumbbell Set', 75.00, 'Adjustable dumbbells 2x10kg', 5,
 '{"material": "steel", "weight": "20kg"}', 8, true, 15, 10, '2025-04-29'),

(18, 'Basketball', 24.50, 'Professional size 7 basketball', 5,
 '{"material": "rubber", "size": "7"}', 8, true, 34, 21, '2025-03-15'),

(19, 'Resistance Bands', 15.99, 'Set of 5 resistance bands', 5,
 '{"tensionLevels": "5", "material": "latex"}', 8, true, 57, 44, '2025-10-09'),

(20, 'Treadmill', 699.00, 'Foldable treadmill for home use', 5,
 '{"maxSpeed": "12km/h", "motorPower": "2HP"}', 8, true, 8, 4, '2025-07-30'),

-- seller 5
(21, 'Fiction Book', 14.99, 'Bestselling mystery novel', 6,
 '{"author": "John Doe", "pages": 320, "language": "English"}', 5, true, 67, 51, '2025-01-07'),

(22, 'Science Textbook', 44.99, 'Physics for university students', 6,
 '{"author": "A. Smith", "edition": "3rd", "language": "English"}', 5, true, 29, 18, '2025-02-19'),

(23, 'Cookbook', 29.99, '100 easy recipes for beginners', 6,
 '{"author": "Emma Clark", "pages": 250}', 5, true, 42, 27, '2025-06-14'),

-- seller 9
(24, 'Japanese Ramen Pack', 9.99, 'Authentic instant ramen from Japan', 7,
 '{"flavor": "miso", "servings": 5, "origin": "Japan"}', 9, true, 84, 61, '2025-04-05'),

(25, 'Matcha Powder', 19.50, 'Premium green tea powder', 7,
 '{"weight": "100g", "origin": "Kyoto"}', 9, true, 33, 22, '2025-08-20'),

(26, 'Sushi Rice', 12.00, 'Short-grain rice for sushi', 7,
 '{"weight": "2kg", "origin": "Japan"}', 9, true, 27, 19, '2025-11-03'),

(27, 'Soy Sauce', 7.50, 'Traditional soy sauce bottle', 7,
 '{"volume": "500ml", "brand": "Kikkoman"}', 9, true, 50, 37, '2025-05-29'),

-- seller 2
(28, 'Car Wax', 15.99, 'Premium car wax for shiny finish', 8,
 '{"volume": "500ml", "type": "liquid"}', 2, true, 40, 23, '2025-09-24'),

(29, 'Air Freshener', 4.99, 'Long-lasting car air freshener', 8,
 '{"scent": "ocean breeze", "duration": "45 days"}', 2, true, 70, 48, '2025-10-02'),

-- seller 6
(30, 'Video Game: Dota Legends', 59.99, 'Action strategy video game', 9,
 '{"genre": "MOBA", "platform": "PC"}', 6, true, 19, 14, '2025-02-22');

SELECT SETVAL('sequence_product', max(id)) FROM product;
