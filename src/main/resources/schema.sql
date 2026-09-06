DROP DATABASE IF EXISTS sgpj;
CREATE DATABASE sgpj;
USE sgpj;

-- =========================================================
-- TABELAS DE DOMÍNIO
-- =========================================================

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

CREATE TABLE microsoft_token_config (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    refresh_token TEXT NULL,
    data_criacao DATETIME NULL
);

-- =========================================================
-- TABELAS BASE
-- =========================================================

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

CREATE TABLE padrao_caminho_armazenamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    caminho_base VARCHAR(255) NOT NULL UNIQUE
);

-- =========================================================
-- USUÁRIO E CARGO
-- =========================================================

CREATE TABLE cargo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(90) NOT NULL UNIQUE,
    criado_por BIGINT,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255),
    cargo_id INT,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo TINYINT NOT NULL DEFAULT 1,

    FOREIGN KEY (cargo_id) REFERENCES cargo(id)
);

ALTER TABLE cargo
ADD CONSTRAINT fk_cargo_usuario
FOREIGN KEY (criado_por) REFERENCES usuario(id);

-- =========================================================
-- CLIENTE
-- =========================================================

CREATE TABLE cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_razao_social VARCHAR(150) NOT NULL,
    cpf_cnpj VARCHAR(14) UNIQUE,
    is_pf TINYINT NOT NULL,
    rg VARCHAR(15),
    status_cliente_id INT NOT NULL,
    endereco_id BIGINT,
    data_hora_envio_formulario DATETIME,
    data_hora_transformacao_em_cliente DATETIME,

    FOREIGN KEY (status_cliente_id) REFERENCES status_cliente(id),
    FOREIGN KEY (endereco_id) REFERENCES endereco(id)
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

ALTER TABLE padrao_caminho_armazenamento
ADD CONSTRAINT fk_padrao_caminho_cliente
FOREIGN KEY (cliente_id) REFERENCES cliente(id);

-- =========================================================
-- PROCESSO API (ALTERADO CONFORME DER)
-- =========================================================

CREATE TABLE processo_dados_api (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_cnj VARCHAR(25) UNIQUE,
    tribunal_tramitacao VARCHAR(100),
    instancia VARCHAR(45),
    data_ajuizamento DATETIME,
    nivel_sigilo VARCHAR(50),
    classe_processual VARCHAR(150),
    formato VARCHAR(15),
    sistema_tribunal VARCHAR(50),
    orgao_julgador VARCHAR(45),
    assunto VARCHAR(45)
);

-- =========================================================
-- MOVIMENTAÇÃO OFICIAL
-- =========================================================

CREATE TABLE movimentacoes_oficiais (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20),
    descricao TEXT,
    data_hora_movimentacao DATETIME,
    processo_dados_api_id BIGINT,

    FOREIGN KEY (processo_dados_api_id)
        REFERENCES processo_dados_api(id)
);

-- =========================================================
-- DOCUMENTO
-- =========================================================

CREATE TABLE documento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    criado_por_usuario_id BIGINT NOT NULL,
    tipo_documento_id INT NOT NULL,
    padrao_caminho_armazenamento_id BIGINT,

    nome_original VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100),
    tamanho_bytes BIGINT,
    hash_arquivo CHAR(64),

    onedrive_drive_id VARCHAR(100),
    onedrive_item_id VARCHAR(200),
    onedrive_web_url VARCHAR(1024),
    onedrive_url_thumbnail TEXT,
    onedrive_url_download TEXT,

    data_hora_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultima_atualizacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ativo TINYINT DEFAULT 1,
    inativado_em DATETIME,
    inativado_por_usuario_id BIGINT,

    FOREIGN KEY (criado_por_usuario_id)
        REFERENCES usuario(id),

    FOREIGN KEY (tipo_documento_id)
        REFERENCES tipo_documento(id),

    FOREIGN KEY (padrao_caminho_armazenamento_id)
        REFERENCES padrao_caminho_armazenamento(id),

    FOREIGN KEY (inativado_por_usuario_id)
        REFERENCES usuario(id)
);

-- =========================================================
-- PROCESSO APLICAÇÃO (ALTERADO CONFORME DER)
-- =========================================================


CREATE TABLE processo_dados_aplicacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    usuario_responsavel_id BIGINT,
    cliente_id BIGINT,

    descricao_caso TEXT,
    observacao TEXT,

    prioridade_id INT,
    fase_processo_id INT,

    ultima_atualizacao DATETIME,
    data_hora_inicio DATETIME,

    ativo TINYINT,

    data_audiencia DATETIME,

    encerrado_em DATETIME,
    encerrado_por_usuario_id BIGINT,

    sigla_tribunal_id INT,

    valor_causa DECIMAL(15,2),

    processo_dados_api_id BIGINT,

    FOREIGN KEY (usuario_responsavel_id)
        REFERENCES usuario(id),

    FOREIGN KEY (cliente_id)
        REFERENCES cliente(id),

    FOREIGN KEY (prioridade_id)
        REFERENCES prioridade(id),

    FOREIGN KEY (fase_processo_id)
        REFERENCES fase_processo(id),

    FOREIGN KEY (encerrado_por_usuario_id)
        REFERENCES usuario(id),

    FOREIGN KEY (sigla_tribunal_id)
        REFERENCES sigla_tribunal(id),

    FOREIGN KEY (processo_dados_api_id)
        REFERENCES processo_dados_api(id)
);

-- =========================================================
-- PROCESSO DOCUMENTO
-- =========================================================

CREATE TABLE processo_documento (
    documento_id BIGINT,
    processo_dados_aplicacao_id BIGINT,
    anexado_por_usuario_id BIGINT,
    anexado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (documento_id, processo_dados_aplicacao_id),

    FOREIGN KEY (documento_id)
        REFERENCES documento(id),

    FOREIGN KEY (processo_dados_aplicacao_id)
        REFERENCES processo_dados_aplicacao(id),

    FOREIGN KEY (anexado_por_usuario_id)
        REFERENCES usuario(id)
);

-- =========================================================
-- HISTÓRICO PROCESSO
-- =========================================================

CREATE TABLE historico_processo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    processo_dados_aplicacao_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,

    descricao_alteracao TEXT,

    data_hora_alteracao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    tipo_alteracao_id INT NOT NULL,

    fase_anterior_id INT,
    fase_nova_id INT,

    FOREIGN KEY (processo_dados_aplicacao_id)
        REFERENCES processo_dados_aplicacao(id),

    FOREIGN KEY (usuario_id)
        REFERENCES usuario(id),

    FOREIGN KEY (tipo_alteracao_id)
        REFERENCES tipo_alteracao(id),

    FOREIGN KEY (fase_anterior_id)
        REFERENCES fase_processo(id),

    FOREIGN KEY (fase_nova_id)
        REFERENCES fase_processo(id)
);

-- =========================================================
-- CARGO PERMISSÃO
-- =========================================================

CREATE TABLE cargo_permissao (
    cargo_id INT,
    permissao_id INT,
    atribuido_por BIGINT,
    data_hora_atribuicao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (cargo_id, permissao_id),

    FOREIGN KEY (cargo_id)
        REFERENCES cargo(id),

    FOREIGN KEY (permissao_id)
        REFERENCES permissao(id),

    FOREIGN KEY (atribuido_por)
        REFERENCES usuario(id)
);

-- =========================================================
-- OTP
-- =========================================================

CREATE TABLE usuario_otp (
    email VARCHAR(255) PRIMARY KEY,
    otp VARCHAR(60) NOT NULL,
    expira_em DATETIME NOT NULL,
    tentativas INT NOT NULL DEFAULT 0
) ENGINE=MEMORY;

-- =========================================================
-- INDEXES
-- =========================================================

CREATE INDEX idx_usuario_cargo_id
ON usuario(cargo_id);

CREATE INDEX idx_usuario_ativo
ON usuario(ativo);

CREATE INDEX idx_cliente_status
ON cliente(status_cliente_id);

CREATE INDEX idx_cliente_endereco
ON cliente(endereco_id);

CREATE INDEX idx_cliente_cpf_cnpj
ON cliente(cpf_cnpj);

CREATE INDEX idx_processo_api_numero_cnj
ON processo_dados_api(numero_cnj);

CREATE INDEX idx_movimentacao_processo
ON movimentacoes_oficiais(processo_dados_api_id);

CREATE INDEX idx_processo_app_usuario
ON processo_dados_aplicacao(usuario_responsavel_id);

CREATE INDEX idx_processo_app_cliente
ON processo_dados_aplicacao(cliente_id);

CREATE INDEX idx_processo_app_fase
ON processo_dados_aplicacao(fase_processo_id);

CREATE INDEX idx_processo_app_api
ON processo_dados_aplicacao(processo_dados_api_id);

-- =========================================================
-- INSERTS BASE
-- =========================================================

INSERT INTO cargo (id, nome)
VALUES (1, 'ADMIN'),
       (2, 'Sem Cargo');

INSERT INTO usuario (nome, email, senha, cargo_id)
VALUES (
    'Thauany Bezerra',
    'thauany@gmail.com',
    '$2a$12$AD1HxQJj.U.dQaam6fntBOCko5p6vrk/6bLLsv79EgpqzqFt7I5D6',
    1
);

INSERT INTO permissao (nome) VALUES
('CRIAR PROCESSO'),
('EDITAR PROCESSO'),
('EXCLUIR PROCESSO'),
('ACEITAR SOLICITAÇÃO'),
('NEGAR SOLICITAÇÃO'),
('CRIAR CLIENTE'),
('EDITAR CLIENTE'),
('EXCLUIR CLIENTE'),
('CRIAR CARGO'),
('EDITAR CARGO'),
('EXCLUIR CARGO'),
('CRIAR USUÁRIO'),
('EDITAR USUÁRIO'),
('EXCLUIR USUÁRIO'),
('CRIAR DOCUMENTO'),
('EDITAR DOCUMENTO'),
('EXCLUIR DOCUMENTO'),
('TODAS AS PERMISSÕES'),
('SEM PERMISSÕES');

INSERT INTO cargo_permissao (cargo_id, permissao_id)
VALUES (1, 18),
       (2, 19);

INSERT INTO status_cliente (nome) VALUES
('LEAD'),
('INATIVO'),
('ATIVO');

INSERT INTO fase_processo (nome) VALUES
('INICIAL'),
('CITAÇÃO'),
('SANEAMENTO'),
('SENTENÇA'),
('CUPRIMENTO DE SENTENÇA'),
('PENHORA');

INSERT INTO sigla_tribunal (sigla_tribunal) VALUES
('tjsp'),
 ('tjal'),
 ('tjap'),
 ('tjam'),
 ('tjba'),
 ('tjce'),
 ('tjdft'),
 ('tjes'),
 ('tjgo'),
 ('tjma'),
 ('tjmt'),
 ('tjms'),
 ('tjmg'),
 ('tjpa'),
 ('tjpb'),
 ('tjpr'),
 ('tjpe'),
 ('tjpi'),
 ('tjrj'),
 ('tjrn'),
 ('tjrs'),
 ('tjro'),
 ('tjrr'),
 ('tjsc'),
 ('tjac'),
 ('tjse'),
 ('tjto');

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



-- =========================================================
-- INSERT COMPLETO DE CLIENTE + FKS
-- =========================================================

INSERT INTO endereco (
    cep,
    logradouro,
    numero,
    bairro,
    cidade,
    estado,
    complemento
)
VALUES (
    '01310-100',
    'Avenida Paulista',
    '1000',
    'Bela Vista',
    'São Paulo',
    'SP',
    'Sala 502'
);

INSERT INTO cliente (
    nome_razao_social,
    cpf_cnpj,
    is_pf,
    rg,
    status_cliente_id,
    endereco_id,
    data_hora_envio_formulario,
    data_hora_transformacao_em_cliente
)
VALUES (
    'João Carlos da Silva',
    '12345678901',
    1,
    '445566778',
    3,
    1,
    NOW(),
    NOW()
);

INSERT INTO contato (
    nome,
    email,
    telefone,
    profissao,
    cliente_id,
    principal
)
VALUES (
    'João Carlos da Silva',
    'joao@email.com',
    '11999999999',
    'Empresário',
    1,
    1
);

use sgpj;

select * from processo_dados_api;
select * from processo_dados_aplicacao;
select * from movimentacoes_oficiais;
select * from historico_processo;