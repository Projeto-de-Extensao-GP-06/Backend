DROP DATABASE IF EXISTS sgpj;
CREATE DATABASE sgpj;
USE sgpj;

-- ============================================================================
-- TABELAS DE DOMÍNIO
-- ============================================================================

CREATE TABLE microsoft_token_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refresh_token TEXT NOT NULL,
    data_criacao DATETIME
);

CREATE TABLE status_cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tipo_documento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE prioridade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL
);

CREATE TABLE fase_processo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE sigla_tribunal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sigla_tribunal VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE tipo_alteracao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(70) NOT NULL UNIQUE
);

CREATE TABLE permissao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL UNIQUE
);

-- ============================================================================
-- TABELAS BASE
-- ============================================================================

CREATE TABLE endereco (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cep VARCHAR(10),
    logradouro VARCHAR(150) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado CHAR(2) NOT NULL,
    complemento VARCHAR(100)
);

CREATE TABLE ultima_movimentacao_oficial (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    descricao TEXT NOT NULL,
    data_hora_movimentacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE padrao_caminho_armazenamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    caminho_base VARCHAR(255) NOT NULL UNIQUE
);

-- ============================================================================
-- USUÁRIO E CARGO
-- ============================================================================

CREATE TABLE cargo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(90) NOT NULL UNIQUE,
    criado_por BIGINT,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NULL,
    cargo_id BIGINT,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo TINYINT NOT NULL DEFAULT 1,

    FOREIGN KEY (cargo_id) REFERENCES cargo(id)
);

ALTER TABLE cargo
ADD CONSTRAINT fk_cargo_usuario
FOREIGN KEY (criado_por) REFERENCES usuario(id);

-- ============================================================================
-- CLIENTE
-- ============================================================================

CREATE TABLE cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_razao_social VARCHAR(150) NOT NULL,
    cpf_cnpj VARCHAR(14) UNIQUE,
    rg VARCHAR(15),
    status_cliente_id BIGINT NOT NULL,
    endereco_id BIGINT,
    data_hora_envio_formulario DATETIME,
    data_hora_transformacao_em_cliente DATETIME,

    FOREIGN KEY (status_cliente_id) REFERENCES status_cliente(id),
    FOREIGN KEY (endereco_id) REFERENCES endereco(id)
);

-- ============================================================================
-- PROCESSO API
-- ============================================================================

CREATE TABLE processo_dados_api (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    ultima_movimentacao_oficial_id BIGINT,

    FOREIGN KEY (ultima_movimentacao_oficial_id)
        REFERENCES ultima_movimentacao_oficial(id)
);

-- ============================================================================
-- DOCUMENTO
-- ============================================================================

CREATE TABLE documento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    criado_por_usuario_id BIGINT NOT NULL,
    tipo_documento_id BIGINT NOT NULL,
    padrao_caminho_armazenamento_id BIGINT,
    nome_original VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,

    onedrive_item_id VARCHAR(255),
    onedrive_url_download TEXT,
    onedrive_url_thumbnail TEXT,

    data_hora_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultima_atualizacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo TINYINT NOT NULL DEFAULT 1,
    inativado_em DATETIME,
    inativado_por_usuario_id BIGINT,

    FOREIGN KEY (criado_por_usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento(id),
    FOREIGN KEY (padrao_caminho_armazenamento_id) REFERENCES padrao_caminho_armazenamento(id),
    FOREIGN KEY (inativado_por_usuario_id) REFERENCES usuario(id)
);

-- ============================================================================
-- PROCESSO APLICAÇÃO
-- ============================================================================

CREATE TABLE processo_dados_aplicacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_responsavel_id BIGINT,
    cliente_id BIGINT NOT NULL,
    descricao_caso TEXT,
    prioridade_id BIGINT,
    fase_processo_id BIGINT NOT NULL,
    ultima_atualizacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_hora_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo TINYINT DEFAULT 1,
    encerrado_em DATETIME,
    encerrado_por_usuario_id BIGINT,
    sigla_tribunal_id BIGINT,
    processo_dados_api_id BIGINT,

    FOREIGN KEY (usuario_responsavel_id) REFERENCES usuario(id),
    FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    FOREIGN KEY (prioridade_id) REFERENCES prioridade(id),
    FOREIGN KEY (fase_processo_id) REFERENCES fase_processo(id),
    FOREIGN KEY (encerrado_por_usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (sigla_tribunal_id) REFERENCES sigla_tribunal(id),
    FOREIGN KEY (processo_dados_api_id) REFERENCES processo_dados_api(id)
);

-- ============================================================================
-- TABELAS RELACIONAIS
-- ============================================================================

CREATE TABLE processo_documento (
    documento_id BIGINT,
    processo_dados_aplicacao_id BIGINT,
    anexado_por_usuario_id BIGINT,
    anexado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (documento_id, processo_dados_aplicacao_id),

    FOREIGN KEY (documento_id) REFERENCES documento(id),
    FOREIGN KEY (processo_dados_aplicacao_id) REFERENCES processo_dados_aplicacao(id),
    FOREIGN KEY (anexado_por_usuario_id) REFERENCES usuario(id)
);

CREATE TABLE historico_processo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    processo_dados_aplicacao_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    descricao_alteracao TEXT,
    data_hora_alteracao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_alteracao_id BIGINT NOT NULL,
    fase_anterior_id BIGINT,
    fase_nova_id BIGINT,

    FOREIGN KEY (processo_dados_aplicacao_id) REFERENCES processo_dados_aplicacao(id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (tipo_alteracao_id) REFERENCES tipo_alteracao(id),
    FOREIGN KEY (fase_anterior_id) REFERENCES fase_processo(id),
    FOREIGN KEY (fase_nova_id) REFERENCES fase_processo(id)
);

CREATE TABLE cargo_permissao (
    cargo_id BIGINT,
    permissao_id BIGINT,
    atribuido_por BIGINT,
    data_hora_atribuicao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (cargo_id, permissao_id),

    FOREIGN KEY (cargo_id) REFERENCES cargo(id),
    FOREIGN KEY (permissao_id) REFERENCES permissao(id),
    FOREIGN KEY (atribuido_por) REFERENCES usuario(id)
);

CREATE TABLE contato (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    profissao VARCHAR(100),
    cliente_id BIGINT NOT NULL,
    principal TINYINT DEFAULT 0,

    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

-- ============================================================================
-- ÍNDICES
-- ============================================================================

-- usuario
CREATE INDEX idx_usuario_cargo_id ON usuario(cargo_id);
CREATE INDEX idx_usuario_ativo ON usuario(ativo);

-- cliente
CREATE INDEX idx_cliente_status ON cliente(status_cliente_id);
CREATE INDEX idx_cliente_endereco ON cliente(endereco_id);
CREATE INDEX idx_cliente_cpf_cnpj ON cliente(cpf_cnpj);

-- processo_dados_api
CREATE INDEX idx_processo_api_numero_cnj ON processo_dados_api(numero_cnj);
CREATE INDEX idx_processo_api_movimentacao ON processo_dados_api(ultima_movimentacao_oficial_id);
CREATE INDEX idx_processo_api_data_ajuizamento ON processo_dados_api(data_ajuizamento);

-- documento
CREATE INDEX idx_documento_usuario ON documento(criado_por_usuario_id);
CREATE INDEX idx_documento_tipo ON documento(tipo_documento_id);
CREATE INDEX idx_documento_caminho ON documento(padrao_caminho_armazenamento_id);
CREATE INDEX idx_documento_ativo ON documento(ativo);
CREATE INDEX idx_documento_inativado_por ON documento(inativado_por_usuario_id);

-- processo_dados_aplicacao
CREATE INDEX idx_processo_app_usuario ON processo_dados_aplicacao(usuario_responsavel_id);
CREATE INDEX idx_processo_app_cliente ON processo_dados_aplicacao(cliente_id);
CREATE INDEX idx_processo_app_prioridade ON processo_dados_aplicacao(prioridade_id);
CREATE INDEX idx_processo_app_fase ON processo_dados_aplicacao(fase_processo_id);
CREATE INDEX idx_processo_app_status_ativo ON processo_dados_aplicacao(ativo);
CREATE INDEX idx_processo_app_sigla ON processo_dados_aplicacao(sigla_tribunal_id);
CREATE INDEX idx_processo_app_api ON processo_dados_aplicacao(processo_dados_api_id);

-- processo_documento
CREATE INDEX idx_proc_doc_processo ON processo_documento(processo_dados_aplicacao_id);
CREATE INDEX idx_proc_doc_usuario ON processo_documento(anexado_por_usuario_id);

-- historico_processo
CREATE INDEX idx_hist_processo ON historico_processo(processo_dados_aplicacao_id);
CREATE INDEX idx_hist_usuario ON historico_processo(usuario_id);
CREATE INDEX idx_hist_tipo ON historico_processo(tipo_alteracao_id);
CREATE INDEX idx_hist_data ON historico_processo(data_hora_alteracao);

-- cargo_permissao
CREATE INDEX idx_cargo_perm_permissao ON cargo_permissao(permissao_id);
CREATE INDEX idx_cargo_perm_usuario ON cargo_permissao(atribuido_por);

-- contato
CREATE INDEX idx_contato_cliente ON contato(cliente_id);
CREATE INDEX idx_contato_principal ON contato(principal);

-- ============================================================================
-- INSERTS INICIAIS
-- ============================================================================

INSERT INTO cargo (nome)
VALUES ('ADMIN');

INSERT INTO usuario (nome, email, senha, cargo_id)
VALUES (
    'Thauany Bezzerra',
    'thauany@gmail.com',
    '$2a$12$Mjj19.b4Gp/JpbHfEHHU6Oh7kSYlT.wofBYNYeWnMWe.A7KelanmO',
    1
);

INSERT INTO permissao (nome) VALUES
('READ'),
('WRITE'),
('DELETE'),
('UPDATE');

INSERT INTO cargo_permissao (cargo_id, permissao_id, atribuido_por)
SELECT 1, id, 1 FROM permissao;

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

-- ============================================================================
-- CRIAÇÃO DO USUÁRIO DA APLICAÇÃO
-- ============================================================================

CREATE USER IF NOT EXISTS 'app_sgpj'@'%' IDENTIFIED BY '1234';
GRANT SELECT, INSERT, UPDATE, DELETE ON sgpj.* TO 'app_sgpj'@'%';
FLUSH PRIVILEGES;

