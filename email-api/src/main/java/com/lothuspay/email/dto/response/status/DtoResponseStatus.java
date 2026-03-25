package com.lothuspay.email.dto.response.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DtoResponseStatus {

    SUCCESS("Sucesso"),
    ERR_AUTH_001("Falha na autenticação"),
    ERR_BODY_001("Corpo da requisição inválido"),

    ERR_INTERNAL_001("Erro interno do servidor")
    ;


    String message;
}
