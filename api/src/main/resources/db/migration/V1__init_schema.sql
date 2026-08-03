-- ==========================================
-- TENANTS (empresas clientes)
-- ==========================================
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    razao_social VARCHAR(150),
    cnpj VARCHAR(20) UNIQUE,
    logradouro VARCHAR(150),
    numero VARCHAR(20),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(2),
    cep VARCHAR(10),
    telefone VARCHAR(20),
    slug VARCHAR(100) NOT NULL UNIQUE,
    url VARCHAR(200) UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- USERS
-- ==========================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    can_sign BOOLEAN DEFAULT FALSE,
    signature_file VARCHAR(100),
    nome_completo VARCHAR(100),
    tenant_id BIGINT,
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- SETORES / LOCAIS DE USO
-- ==========================================
CREATE TABLE setores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tenant_id BIGINT,
    CONSTRAINT fk_setores_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE locais_uso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tenant_id BIGINT,
    CONSTRAINT fk_locais_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- PROCEDIMENTOS (Instruções de Calibração)
-- ==========================================
CREATE TABLE procedimentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50),
    nome VARCHAR(100),
    texto_base TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- PADRÕES METROLÓGICOS
-- ==========================================
CREATE TABLE padroes_metrologicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identificacao VARCHAR(50) NOT NULL,
    descricao_detalhada TEXT NOT NULL,
    certificado_origem VARCHAR(100),
    data_validade DATE,
    ativo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- INSTRUMENTOS
-- ==========================================
CREATE TABLE instrumentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    numero_sequencial VARCHAR(20),
    identificacao VARCHAR(50) NOT NULL,
    descricao VARCHAR(150) NOT NULL,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    capacidade VARCHAR(50),
    precisao VARCHAR(50),
    setor_id BIGINT,
    local_id BIGINT,
    frequencia_meses INT DEFAULT 12,
    data_cadastro DATE,
    data_certificacao DATE,
    certificado VARCHAR(50),
    data_proxima_calibracao DATE,

    lp_min DOUBLE,
    lp_max DOUBLE,
    aferido_lp DOUBLE,
    situacao_lp VARCHAR(50),

    lnp_min DOUBLE,
    lnp_max DOUBLE,
    aferido_lnp DOUBLE,
    situacao_lnp VARCHAR(50),

    erro DOUBLE,
    incerteza_medicao DOUBLE,
    criterio_aceitacao DOUBLE,
    erro_quadratico DOUBLE,
    incerteza_quadratica DOUBLE,
    soma_quadratica DOUBLE,
    resultado_mafra VARCHAR(50),
    prox_calib_formatada VARCHAR(20),

    status_geral VARCHAR(50),
    observacoes TEXT,

    document_hash VARCHAR(64),
    assinatura_data DATETIME,
    assinante_nome VARCHAR(100),
    assinante_ip VARCHAR(45),

    metrologista_nome VARCHAR(100),
    metrologista_data DATETIME,
    metrologista_sig VARCHAR(100),

    responsavel_nome VARCHAR(100),
    responsavel_data DATETIME,
    responsavel_sig VARCHAR(100),

    layout_pdf VARCHAR(30) DEFAULT 'padrao',
    pdf_fisico VARCHAR(100),
    procedimento_id BIGINT,

    CONSTRAINT fk_inst_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_inst_setor FOREIGN KEY (setor_id) REFERENCES setores(id),
    CONSTRAINT fk_inst_local FOREIGN KEY (local_id) REFERENCES locais_uso(id),
    CONSTRAINT fk_inst_procedimento FOREIGN KEY (procedimento_id) REFERENCES procedimentos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- HISTÓRICO DE CALIBRAÇÕES
-- ==========================================
CREATE TABLE historico_calibracoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instrumento_id BIGINT NOT NULL,
    data_certificacao DATE,
    certificado VARCHAR(50),
    erro DOUBLE,
    incerteza DOUBLE,
    status_na_epoca VARCHAR(50),
    pdf_fisico VARCHAR(100),
    CONSTRAINT fk_hist_instrumento FOREIGN KEY (instrumento_id) REFERENCES instrumentos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- PONTOS DE CALIBRAÇÃO (grade de resultados)
-- ==========================================
CREATE TABLE pontos_calibracao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instrumento_id BIGINT NOT NULL,
    ponto_nominal VARCHAR(20),
    v_indicado_1 DOUBLE,
    v_indicado_2 DOUBLE,
    v_indicado_3 DOUBLE,
    erro_calculado DOUBLE,
    ordem INT DEFAULT 0,
    CONSTRAINT fk_ponto_instrumento FOREIGN KEY (instrumento_id) REFERENCES instrumentos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- TABELA N:N Instrumento <-> Padrão
-- ==========================================
CREATE TABLE instrumento_padrao (
    instrumento_id BIGINT NOT NULL,
    padrao_id BIGINT NOT NULL,
    PRIMARY KEY (instrumento_id, padrao_id),
    CONSTRAINT fk_ip_instrumento FOREIGN KEY (instrumento_id) REFERENCES instrumentos(id) ON DELETE CASCADE,
    CONSTRAINT fk_ip_padrao FOREIGN KEY (padrao_id) REFERENCES padroes_metrologicos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- AUDIT LOGS
-- ==========================================
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(120),
    action VARCHAR(100),
    details TEXT,
    tenant_id BIGINT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    CONSTRAINT fk_log_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;