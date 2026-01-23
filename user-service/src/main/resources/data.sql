-- Utenti di test per il login
-- Password in chiaro per tutti: password123
-- Hash generato con BCrypt (strength 10)

INSERT INTO users (username, email, password_hash, first_name, last_name, enabled, created_at, updated_at)
SELECT 'mario', 'mario.rossi@test.it', '$2a$10$knIGHwyOj58EJjA0k7FT8OhQxaIYekF3qEJQlJ6QIkeEzJbgFpCeC',
       'Mario', 'Rossi', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'mario');

INSERT INTO users (username, email, password_hash, first_name, last_name, enabled, created_at, updated_at)
SELECT 'luigi', 'luigi.verdi@test.it', '$2a$10$knIGHwyOj58EJjA0k7FT8OhQxaIYekF3qEJQlJ6QIkeEzJbgFpCeC',
       'Luigi', 'Verdi', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'luigi');

INSERT INTO users (username, email, password_hash, first_name, last_name, enabled, created_at, updated_at)
SELECT 'anna', 'anna.bianchi@test.it', '$2a$10$knIGHwyOj58EJjA0k7FT8OhQxaIYekF3qEJQlJ6QIkeEzJbgFpCeC',
       'Anna', 'Bianchi', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'anna');

INSERT INTO users (username, email, password_hash, first_name, last_name, enabled, created_at, updated_at)
SELECT 'paolo', 'paolo.neri@test.it', '$2a$10$knIGHwyOj58EJjA0k7FT8OhQxaIYekF3qEJQlJ6QIkeEzJbgFpCeC',
       'Paolo', 'Neri', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'paolo');

INSERT INTO users (username, email, password_hash, first_name, last_name, enabled, created_at, updated_at)
SELECT 'giulia', 'giulia.gialli@test.it', '$2a$10$knIGHwyOj58EJjA0k7FT8OhQxaIYekF3qEJQlJ6QIkeEzJbgFpCeC',
       'Giulia', 'Gialli', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'giulia');
