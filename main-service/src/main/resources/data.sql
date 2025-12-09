-- Тестовые пользователи
INSERT INTO users (name, email) VALUES
('John Doe', 'john@example.com'),
('Jane Smith', 'jane@example.com'),
('Bob Johnson', 'bob@example.com')
ON CONFLICT (email) DO NOTHING;

-- Тестовые категории
INSERT INTO categories (name) VALUES
('Концерты'),
('Выставки'),
('Спорт'),
('Образование'),
('Развлечения')
ON CONFLICT (name) DO NOTHING;