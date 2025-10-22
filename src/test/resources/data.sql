-- No crear base de datos ni usar 'USE'

-- Tabla customers
CREATE TABLE IF NOT EXISTS customers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  phone VARCHAR(25),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla addresses
CREATE TABLE IF NOT EXISTS addresses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  line1 VARCHAR(160) NOT NULL,
  line2 VARCHAR(160),
  city VARCHAR(80) NOT NULL,
  postal_code VARCHAR(20) NOT NULL,
  country VARCHAR(80) NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_address_customer ON addresses(customer_id);

-- Tabla products
CREATE TABLE IF NOT EXISTS products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sku VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(160) NOT NULL,
  description VARCHAR(2000),
  price DECIMAL(12,2) NOT NULL,
  stock INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_name ON products(name);

-- Tabla orders
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  shipping_address_id BIGINT NOT NULL,
  order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(20) NOT NULL,
  total DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_order_shipping_address FOREIGN KEY (shipping_address_id) REFERENCES addresses(id)
);

CREATE INDEX IF NOT EXISTS idx_order_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_date ON orders(order_date);

-- Tabla order_items
CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX IF NOT EXISTS idx_orderitem_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_orderitem_product ON order_items(product_id);

-- Datos de ejemplo
INSERT INTO customers(full_name, email, phone) VALUES
  ('John Doe', 'john.doe@example.com', '+34 600 111 222'),
  ('Jane Smith', 'jane.smith@example.com', '+34 600 333 444');

INSERT INTO addresses(customer_id, line1, line2, city, postal_code, country, is_default) VALUES
  (1, 'Calle Mayor 1', NULL, 'Madrid', '28013', 'España', TRUE),
  (1, 'Avenida Prado 10', 'Piso 2B', 'Madrid', '28014', 'España', FALSE),
  (2, 'Gran Vía 123', NULL, 'Madrid', '28010', 'España', TRUE);

INSERT INTO products(sku, name, description, price, stock, active) VALUES
  ('P-100', 'Café Premium 250g', 'Mezcla arábica tostado medio', 7.50, 100, TRUE),
  ('P-200', 'Taza Cerámica', 'Taza 300ml apta para lavavajillas', 12.00, 50, TRUE),
  ('P-300', 'Pack Galletas 6u', 'Galletas artesanas surtidas', 3.20, 300, TRUE);

INSERT INTO orders(customer_id, shipping_address_id, order_date, status, total) VALUES
  (1, 1, CURRENT_TIMESTAMP(), 'CREATED', 27.00),
  (2, 3, CURRENT_TIMESTAMP(), 'CREATED', 16.00);

INSERT INTO order_items(order_id, product_id, quantity, unit_price) VALUES
  (1, 1, 2, 7.50),
  (1, 2, 1, 12.00),
  (2, 3, 5, 3.20);
