INSERT INTO product(id, name, price, description, category_id, characteristic, seller_id, is_visible)
VALUES
-- seller 6

(1, 'Gaming Laptop', 1199.99, 'High performance gaming laptop', 1,
 '{"brand": "ASUS", "RAM": "16GB", "GPU": "RTX 3060", "storage": "1TB SSD"}', 6, true),

(2, 'Mechanical Keyboard', 89.99, 'RGB mechanical keyboard with blue switches', 1,
 '{"brand": "Keychron", "switches": "blue", "layout": "ANSI"}', 6, true),

(3, 'Wireless Mouse', 24.99, 'Ergonomic wireless mouse with silent clicks', 1,
 '{"brand": "Logitech", "battery": "AA", "dpi": "1600"}', 6, true),

(4, '4K Monitor', 299.99, 'Ultra HD 4K monitor 27-inch', 1,
 '{"brand": "Samsung", "resolution": "3840x2160", "refreshRate": "75Hz"}', 6, true),

(5, 'External SSD', 129.99, 'Fast external SSD 1TB', 1,
 '{"brand": "SanDisk", "speed": "1050MB/s", "capacity": "1TB"}', 6, true),

-- seller 2
(6, 'Men Hoodie', 45.50, 'Warm cotton hoodie for men', 2,
 '{"color": "black", "size": "L", "material": "cotton"}', 2, true),

(7, 'Women Jacket', 79.99, 'Lightweight jacket for spring', 2,
 '{"color": "beige", "size": "M", "material": "polyester"}', 2, true),

(8, 'Sneakers', 99.00, 'Running sneakers with breathable mesh', 2,
 '{"brand": "Nike", "size": "42", "color": "white"}', 2, true),

(9, 'Baseball Cap', 19.99, 'Adjustable unisex cap', 2,
 '{"color": "navy", "material": "cotton"}', 2, true),

(10, 'Jeans', 59.90, 'Classic blue jeans', 2,
 '{"size": "32", "fit": "regular", "color": "blue"}', 2, true),

-- seller 2
(11, 'LED Lamp', 35.00, 'Energy saving LED lamp with warm light', 3,
 '{"power": "15W", "colorTemp": "3000K"}', 2, true),

(12, 'Wall Clock', 22.50, 'Minimalist wall clock', 3,
 '{"diameter": "30cm", "material": "wood"}', 2, true),

-- seller 2
(13, 'Knife Set', 89.00, 'Set of 5 stainless steel knives', 4,
 '{"blades": "stainless steel", "pieces": 5, "handle": "wood"}', 2, true),

(14, 'Blender', 49.99, '500W blender with glass jug', 4,
 '{"power": "500W", "capacity": "1.5L"}', 2, true),

(15, 'Non-stick Pan', 34.99, 'Frying pan with non-stick coating', 4,
 '{"diameter": "28cm", "material": "aluminum"}', 2, true),

-- seller 8
(16, 'Yoga Mat', 29.99, 'Non-slip yoga mat 6mm', 5,
 '{"color": "purple", "length": "180cm"}', 8, true),

(17, 'Dumbbell Set', 75.00, 'Adjustable dumbbells 2x10kg', 5,
 '{"material": "steel", "weight": "20kg"}', 8, true),

(18, 'Basketball', 24.50, 'Professional size 7 basketball', 5,
 '{"material": "rubber", "size": "7"}', 8, true),

(19, 'Resistance Bands', 15.99, 'Set of 5 resistance bands', 5,
 '{"tensionLevels": "5", "material": "latex"}', 8, true),

(20, 'Treadmill', 699.00, 'Foldable treadmill for home use', 5,
 '{"maxSpeed": "12km/h", "motorPower": "2HP"}', 8, true),

-- seller 5
(21, 'Fiction Book', 14.99, 'Bestselling mystery novel', 6,
 '{"author": "John Doe", "pages": 320, "language": "English"}', 5, true),

(22, 'Science Textbook', 44.99, 'Physics for university students', 6,
 '{"author": "A. Smith", "edition": "3rd", "language": "English"}', 5, true),

(23, 'Cookbook', 29.99, '100 easy recipes for beginners', 6,
 '{"author": "Emma Clark", "pages": 250}', 5, true),

-- seller 9
(24, 'Japanese Ramen Pack', 9.99, 'Authentic instant ramen from Japan', 7,
 '{"flavor": "miso", "servings": 5, "origin": "Japan"}', 9, true),

(25, 'Matcha Powder', 19.50, 'Premium green tea powder', 7,
 '{"weight": "100g", "origin": "Kyoto"}', 9, true),

(26, 'Sushi Rice', 12.00, 'Short-grain rice for sushi', 7,
 '{"weight": "2kg", "origin": "Japan"}', 9, true),

(27, 'Soy Sauce', 7.50, 'Traditional soy sauce bottle', 7,
 '{"volume": "500ml", "brand": "Kikkoman"}', 9, true),

-- seller 2
(28, 'Car Wax', 15.99, 'Premium car wax for shiny finish', 8,
 '{"volume": "500ml", "type": "liquid"}', 2, true),

(29, 'Air Freshener', 4.99, 'Long-lasting car air freshener', 8,
 '{"scent": "ocean breeze", "duration": "45 days"}', 2, true),

-- seller 6
(30, 'Video Game: Dota Legends', 59.99, 'Action strategy video game', 9,
 '{"genre": "MOBA", "platform": "PC"}', 6, true);

SELECT SETVAL('sequence_product', max(id)) FROM product;
