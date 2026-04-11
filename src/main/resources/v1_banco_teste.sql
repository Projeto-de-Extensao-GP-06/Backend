DROP DATABASE IF EXISTS sgpj;
CREATE DATABASE sgpj;
USE sgpj;

CREATE TABLE usuario (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         nome VARCHAR(100) NOT NULL,
                         email VARCHAR(100) NOT NULL UNIQUE,
                         senha VARCHAR(250) NOT NULL
);

INSERT INTO usuario
(nome, email, senha)
values
    ('John Doe', 'john@doe.com', '$2a$10$0/TKTGxdREbWaWjWYhwf6e9P1fPOAMMNqEnZgOG95jnSkHSfkkIrC');
-- senha: 123456

CREATE USER 'admin_sgpj'@'%' IDENTIFIED WITH mysql_native_password BY '1234';
GRANT ALL PRIVILEGES ON sgpj.* TO 'admin_sgpj'@'%';
FLUSH PRIVILEGES;