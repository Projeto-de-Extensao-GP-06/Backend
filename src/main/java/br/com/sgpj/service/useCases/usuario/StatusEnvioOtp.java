package br.com.sgpj.service.useCases.usuario;

public enum StatusEnvioOtp {
    ENVIADO,
    USUARIO_NAO_ENCONTRADO,
    USUARIO_INATIVO,
    SENHA_JA_CADASTRADA,
    SENHA_NAO_CADASTRADA
}
