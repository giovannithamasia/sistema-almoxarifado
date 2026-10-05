CREATE DATABASE saep_db;
USE saep_db;

CREATE TABLE usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL,
    login VARCHAR(45) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    papel ENUM('ROLE_ADMIN', 'ROLE_USER') NOT NULL
);

CREATE TABLE produto (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(45) NOT NULL UNIQUE,
    nome VARCHAR(45) NOT NULL,
    caracteristicas VARCHAR(45) NOT NULL,
    estoque_atual INT NOT NULL,
    estoque_minimo INT NOT NULL
);

CREATE TABLE movimentacao_estoque (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_produto BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    tipo_movimentacao ENUM('ENTRADA', 'SAIDA') NOT NULL,
    quantidade INT NOT NULL,
    data_movimentacao DATETIME NOT NULL,
    FOREIGN KEY (id_produto) REFERENCES produto(id),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id)
);

INSERT INTO usuario (nome, login, senha, papel) VALUES
 ('Giovanni', 'giovanni', 'gio123', 'ROLE_ADMIN'),
 ('Maria', 'maria', 'maria123', 'ROLE_USER'),
 ('João', 'joao', 'jo123', 'ROLE_USER');

INSERT INTO produto (codigo, nome, caracteristicas, estoque_atual, estoque_minimo) VALUES
('FRM-001', 'Martelo Unha', 'Cabo de madeira, cabeca de aco', 50, 10),
('FRM-002', 'Chave de Fenda', 'Ponta imantada, cabo isolante', 80, 15),
('FRM-003', 'Alicate Universal', 'Cabo emborrachado, 8 polegadas', 30, 5);

INSERT INTO movimentacao_estoque (id_produto, id_usuario, tipo_movimentacao, quantidade, data_movimentacao) VALUES
(1, 1, 'ENTRADA', 20, '2026-08-10 10:00:00'),
(2, 2, 'SAIDA', 2, '2026-08-10 11:30:00'),
(3, 3, 'ENTRADA', 40, '2026-08-10 14:15:00');