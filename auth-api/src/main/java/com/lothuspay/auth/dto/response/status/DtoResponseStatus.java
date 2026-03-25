package com.lothuspay.auth.dto.response.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DtoResponseStatus {

    SUCCESS("Sucesso"),

    ERR_AUTH_001("E-mail já registrado"),
    ERR_AUTH_002("Telefone já registrado"),

    ERR_AUTH_003("A senha deve ter no mínimo 8 caracteres, conter pelo menos uma letra maiúscula, uma letra minúscula, um número e um caractere especial (#@!)"),
    ERR_AUTH_004("Número de documento já registrado"),
    ERR_AUTH_005("O tipo de documento deve ser CPF ou CNPJ"),

    ERR_AUTH_006("E-mail ou senha inválidos"),
    ERR_AUTH_007("Conta não encontrada"),

    ERR_AUTH_008("Token inválido"),
    ERR_AUTH_009("Acesso não autorizado"),
    ERR_AUTH_010("Número de documento inválido"),
    ERR_AUTH_011("Número de telefone inválido"),

    ERR_AUTH_020("Erro ao gerar o token"),
    ERR_AUTH_021("Token não é válido"),
    ERR_AUTH_023("2FA já está habilitado"),
    ERR_AUTH_024("2FA não está habilitado"),

    ERR_APIKEY_001("Chave de API não encontrada"),
    ERR_APIKEY_002("Chave de API inválida"),
    ERR_APIKEY_003("Chave de API revogada"),
    ERR_APIKEY_004("Segredo da API inválido"),

    ERR_KYC_001("Documentos de KYC são obrigatórios"),
    ERR_KYC_002("KYC já enviado"),
    ERR_KYC_003("KYC não encontrado"),

    ERR_KYC_100("Erro ao enviar os documentos de KYC")
    ;

    String message;
}
