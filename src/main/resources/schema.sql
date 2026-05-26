DROP DATABASE IF EXISTS sgpj;
CREATE DATABASE sgpj;
USE sgpj;

-- ============================================================================
-- TABELAS DE DOMÍNIO
-- ============================================================================

CREATE TABLE microsoft_token_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    refresh_token TEXT NOT NULL,
    data_criacao  DATETIME
);

CREATE TABLE status_cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tipo_documento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE prioridade (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL
);

CREATE TABLE fase_processo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE sigla_tribunal (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sigla_tribunal VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE tipo_alteracao (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(70) NOT NULL UNIQUE
);

CREATE TABLE permissao (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL UNIQUE
);

-- ============================================================================
-- TABELAS BASE
-- ============================================================================

CREATE TABLE endereco (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cep VARCHAR(10),
    logradouro VARCHAR(150) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado CHAR(2) NOT NULL,
    complemento VARCHAR(100)
);

CREATE TABLE ultima_movimentacao_oficial (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    descricao TEXT NOT NULL,
    data_hora_movimentacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================================
-- USUÁRIO E CARGO
-- ============================================================================

CREATE TABLE cargo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(90) NOT NULL UNIQUE,
    criado_por INT,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NULL,
    cargo_id INT,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo TINYINT NOT NULL DEFAULT 1,

    FOREIGN KEY (cargo_id) REFERENCES cargo (id)
);

ALTER TABLE cargo
    ADD CONSTRAINT fk_cargo_usuario
        FOREIGN KEY (criado_por) REFERENCES usuario (id);

-- ============================================================================
-- CLIENTE
-- ============================================================================

CREATE TABLE cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_razao_social VARCHAR(150) NOT NULL,
    cpf_cnpj VARCHAR(14) UNIQUE,
    rg VARCHAR(15),
    status_cliente_id INT NOT NULL,
    endereco_id INT,
    data_hora_envio_formulario DATETIME,
    data_hora_transformacao_em_cliente DATETIME,

    FOREIGN KEY (status_cliente_id) REFERENCES status_cliente(id),
    FOREIGN KEY (endereco_id) REFERENCES endereco(id)
);

-- ============================================================================
-- CAMINHO DE ARMAZENAMENTO (depende de cliente)
-- ============================================================================

CREATE TABLE padrao_caminho_armazenamento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT,
    caminho_base VARCHAR(150) NOT NULL UNIQUE,

    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

-- ============================================================================
-- PROCESSO API
-- ============================================================================

CREATE TABLE processo_dados_api (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_cnj VARCHAR(25) UNIQUE,
    tribunal_tramitacao VARCHAR(100),
    parte_adversa VARCHAR(150),
    data_audiencia DATETIME,
    valor_causa DECIMAL(15,2),
    observacao TEXT,
    instancia VARCHAR(45),
    data_ajuizamento DATETIME,
    nivel_sigilo VARCHAR(50),
    classe_processual VARCHAR(150),
    nome_serventia VARCHAR(200),
    comarca VARCHAR(100),
    formato VARCHAR(15),
    sistema_tribunal VARCHAR(50),
    ultima_movimentacao_oficial_id INT,

    FOREIGN KEY (ultima_movimentacao_oficial_id)
        REFERENCES ultima_movimentacao_oficial (id)
);

-- ============================================================================
-- DOCUMENTO
-- ============================================================================

CREATE TABLE documento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    criado_por_usuario_id INT NOT NULL,
    tipo_documento_id INT NOT NULL,
    padrao_caminho_armazenamento_id INT,
    nome_original VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    tamanho_bytes INT NOT NULL,

    onedrive_item_id                VARCHAR(255),
    onedrive_url_download           TEXT,
    onedrive_url_thumbnail          TEXT,

    data_hora_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultima_atualizacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo TINYINT NOT NULL DEFAULT 1,
    inativado_em DATETIME,
    inativado_por_usuario_id INT,

    FOREIGN KEY (criado_por_usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento (id),
    FOREIGN KEY (padrao_caminho_armazenamento_id) REFERENCES padrao_caminho_armazenamento (id),
    FOREIGN KEY (inativado_por_usuario_id) REFERENCES usuario (id)
);

-- ============================================================================
-- PROCESSO APLICAÇÃO
-- ============================================================================

CREATE TABLE processo_dados_aplicacao (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_responsavel_id INT,
    cliente_id INT NOT NULL,
    descricao_caso TEXT,
    prioridade_id INT,
    fase_processo_id INT NOT NULL,
    ultima_atualizacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_hora_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo TINYINT DEFAULT 1,
    encerrado_em DATETIME,
    encerrado_por_usuario_id INT,
    sigla_tribunal_id INT,
    processo_dados_api_id INT,

    FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario (id),
    FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    FOREIGN KEY (prioridade_id) REFERENCES prioridade (id),
    FOREIGN KEY (fase_processo_id) REFERENCES fase_processo (id),
    FOREIGN KEY (encerrado_por_usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (sigla_tribunal_id) REFERENCES sigla_tribunal (id),
    FOREIGN KEY (processo_dados_api_id) REFERENCES processo_dados_api (id)
);

-- ============================================================================
-- TABELAS RELACIONAIS
-- ============================================================================

CREATE TABLE processo_documento (
    documento_id INT,
    processo_dados_aplicacao_id INT,
    anexado_por_usuario_id INT,
    anexado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (documento_id, processo_dados_aplicacao_id),

    FOREIGN KEY (documento_id) REFERENCES documento (id),
    FOREIGN KEY (processo_dados_aplicacao_id) REFERENCES processo_dados_aplicacao (id),
    FOREIGN KEY (anexado_por_usuario_id) REFERENCES usuario (id)
);

CREATE TABLE historico_processo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    processo_dados_aplicacao_id INT NOT NULL,
    usuario_id INT NOT NULL,
    descricao_alteracao TEXT,
    data_hora_alteracao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_alteracao_id INT NOT NULL,
    fase_anterior_id INT,
    fase_nova_id INT,

    FOREIGN KEY (processo_dados_aplicacao_id) REFERENCES processo_dados_aplicacao (id),
    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (tipo_alteracao_id) REFERENCES tipo_alteracao (id),
    FOREIGN KEY (fase_anterior_id) REFERENCES fase_processo (id),
    FOREIGN KEY (fase_nova_id) REFERENCES fase_processo (id)
);

CREATE TABLE cargo_permissao (
    cargo_id INT,
    permissao_id INT,
    atribuido_por INT,
    data_hora_atribuicao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (cargo_id, permissao_id),

    FOREIGN KEY (cargo_id) REFERENCES cargo (id),
    FOREIGN KEY (permissao_id) REFERENCES permissao (id),
    FOREIGN KEY (atribuido_por) REFERENCES usuario (id)
);

CREATE TABLE contato (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    profissao VARCHAR(100),
    cliente_id INT NOT NULL,
    principal TINYINT DEFAULT 0,

    FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

-- ============================================================================
-- TABELAS EM MEMÓRIA
-- ============================================================================

CREATE TABLE usuario_otp
(
    email      VARCHAR(255) PRIMARY KEY,
    otp        VARCHAR(6) NOT NULL,
    expira_em  DATETIME   NOT NULL,
    tentativas INT        NOT NULL DEFAULT 0
) ENGINE=MEMORY;

-- ============================================================================
-- ÍNDICES
-- ============================================================================

-- usuario
CREATE INDEX idx_usuario_cargo_id ON usuario (cargo_id);
CREATE INDEX idx_usuario_ativo ON usuario (ativo);

-- cliente
CREATE INDEX idx_cliente_status ON cliente(status_cliente_id);
CREATE INDEX idx_cliente_endereco ON cliente(endereco_id);
CREATE INDEX idx_cliente_cpf_cnpj ON cliente(cpf_cnpj);

-- padrao_caminho_armazenamento
CREATE INDEX idx_caminho_cliente ON padrao_caminho_armazenamento(cliente_id);

-- processo_dados_api
CREATE INDEX idx_processo_api_numero_cnj ON processo_dados_api (numero_cnj);
CREATE INDEX idx_processo_api_movimentacao ON processo_dados_api (ultima_movimentacao_oficial_id);
CREATE INDEX idx_processo_api_data_ajuizamento ON processo_dados_api (data_ajuizamento);

-- documento
CREATE INDEX idx_documento_usuario ON documento (criado_por_usuario_id);
CREATE INDEX idx_documento_tipo ON documento (tipo_documento_id);
CREATE INDEX idx_documento_caminho ON documento (padrao_caminho_armazenamento_id);
CREATE INDEX idx_documento_ativo ON documento (ativo);
CREATE INDEX idx_documento_inativado_por ON documento (inativado_por_usuario_id);

-- processo_dados_aplicacao
CREATE INDEX idx_processo_app_usuario ON processo_dados_aplicacao (usuario_responsavel_id);
CREATE INDEX idx_processo_app_cliente ON processo_dados_aplicacao (cliente_id);
CREATE INDEX idx_processo_app_prioridade ON processo_dados_aplicacao (prioridade_id);
CREATE INDEX idx_processo_app_fase ON processo_dados_aplicacao (fase_processo_id);
CREATE INDEX idx_processo_app_status_ativo ON processo_dados_aplicacao (ativo);
CREATE INDEX idx_processo_app_sigla ON processo_dados_aplicacao (sigla_tribunal_id);
CREATE INDEX idx_processo_app_api ON processo_dados_aplicacao (processo_dados_api_id);

-- processo_documento
CREATE INDEX idx_proc_doc_processo ON processo_documento (processo_dados_aplicacao_id);
CREATE INDEX idx_proc_doc_usuario ON processo_documento (anexado_por_usuario_id);

-- historico_processo
CREATE INDEX idx_hist_processo ON historico_processo (processo_dados_aplicacao_id);
CREATE INDEX idx_hist_usuario ON historico_processo (usuario_id);
CREATE INDEX idx_hist_tipo ON historico_processo (tipo_alteracao_id);
CREATE INDEX idx_hist_data ON historico_processo (data_hora_alteracao);

-- cargo_permissao
CREATE INDEX idx_cargo_perm_permissao ON cargo_permissao (permissao_id);
CREATE INDEX idx_cargo_perm_usuario ON cargo_permissao (atribuido_por);

-- contato
CREATE INDEX idx_contato_cliente ON contato (cliente_id);
CREATE INDEX idx_contato_principal ON contato (principal);

-- ============================================================================
-- INSERTS INICIAIS
-- ============================================================================

INSERT INTO cargo (nome)
VALUES ('ADMIN');

INSERT INTO usuario (nome, email, senha, cargo_id)
VALUES ('Thauany Bezzerra',
        'thauany@gmail.com',
        '$2a$12$Mjj19.b4Gp/JpbHfEHHU6Oh7kSYlT.wofBYNYeWnMWe.A7KelanmO',
        1);

INSERT INTO permissao (nome) VALUES
    ('READ'),
    ('WRITE'),
    ('DELETE'),
    ('UPDATE');

INSERT INTO cargo_permissao (cargo_id, permissao_id, atribuido_por)
SELECT 1, id, 1
FROM permissao;

INSERT INTO status_cliente (nome) VALUES
    ('LEAD'),
    ('INATIVO'),
    ('ATIVO');

INSERT INTO fase_processo (nome) VALUES
    ('INICIAL'),
    ('CITAÇÃO'),
    ('SANEAMENTO'),
    ('SENTENÇA'),
    ('CUMPRIMENTO DE SENTENÇA'),
    ('PENHORA');

INSERT INTO sigla_tribunal (sigla_tribunal) VALUES
    ('tjsp'),
    ('tjrg'),
    ('tjpr');

INSERT INTO prioridade (nome) VALUES
    ('ALTA'),
    ('MEDIA'),
    ('BAIXA');

INSERT INTO tipo_documento (nome) VALUES
    ('RG'),
    ('DOCUMENTO DE DEFESA'),
    ('COMPROVANTE DE ENDEREÇO'),
    ('CTPS'),
    ('PROCURAÇÃO'),
    ('OUTROS');

INSERT INTO tipo_alteracao (nome) VALUES
    ('ABERTURA DE PROCESSO'),
    ('FECHAMENTO DE PROCESSO'),
    ('MUDANÇA DE STATUS'),
    ('ADIÇÃO DE DOCUMENTO'),
    ('ADIÇÃO DE ARQUIVO'),
    ('OBSERVAÇÃO'),
    ('ATUALIZAÇÃO DE DADOS'),
    ('ALTERAÇÃO DE RESPONSÁVEL');

INSERT INTO microsoft_token_config (refresh_token) VALUES
('REFRESH_TOKEN_AQUI');


INSERT INTO endereco (cep, logradouro, numero, bairro, cidade, estado, complemento)
VALUES
('01310-200', 'Avenida Paulista', '1000', 'Bela Vista', 'São Paulo', 'SP', 'Apto 42'),
('20040-002', 'Avenida Rio Branco', '156', 'Centro', 'Rio de Janeiro', 'RJ', 'Sala 801');

INSERT INTO cliente (nome_razao_social, cpf_cnpj, rg, status_cliente_id, endereco_id, data_hora_envio_formulario, data_hora_transformacao_em_cliente)
VALUES ('Rafael Silva', '12345678901', '123456789', 1, 1, '2026-05-10 14:30:00', NULL);

INSERT INTO cliente (nome_razao_social, cpf_cnpj, rg, status_cliente_id, endereco_id, data_hora_envio_formulario, data_hora_transformacao_em_cliente)
VALUES ('Tech Solutions LTDA', '98765432100012', NULL, 2, 2, '2026-04-01 09:00:00', '2026-04-03 11:15:00');

INSERT INTO contato (nome, email, telefone, profissao, cliente_id, principal)
VALUES
    ('Rafael Silva', 'rafael@email.com', '(11) 99999-9999', 'Desenvolvedor', 1, 1),
    ('Carlos Souza', 'carlos@techsolutions.com', '(21) 98888-8888', 'Gerente de TI', 2, 1),
    ('Ana Costa', 'suporte@techsolutions.com', '(21) 2222-2222', 'Analista de Suporte', 2, 0);

-- ============================================================================
-- CRIAÇÃO DO USUÁRIO DA APLICAÇÃO
-- ============================================================================

CREATE
USER IF NOT EXISTS 'app_sgpj'@'%' IDENTIFIED BY '1234';
GRANT
SELECT,
INSERT
,
UPDATE,
DELETE
ON sgpj.* TO 'app_sgpj'@'%';
FLUSH
PRIVILEGES;

