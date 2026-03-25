package com.lothuspay.wallet.dto.response.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DtoResponseStatus {

    SUCCESS("Sucesso"),

    ERR_AUTH_001("Falha na autenticação"),

    ERR_WALLET_001("Carteira não encontrada"),

    ERR_DEPOSIT_001("Depósito não encontrado"),
    ERR_WITHDRAWAL_001("Saque não encontrado"),

    ;

    String message;
}
