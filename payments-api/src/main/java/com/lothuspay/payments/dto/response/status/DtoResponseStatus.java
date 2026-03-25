package com.lothuspay.payments.dto.response.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DtoResponseStatus {

    SUCCESS("Sucesso"),

    ERR_AUTH_001("Falha na autenticação"),

    ERR_WALLET_001("Carteira não encontrada"),

    ERR_DEPOSIT_000("Depósitos em manutenção, tente novamente mais tarde"),
    ERR_DEPOSIT_001("Depósito não encontrado"),
    ERR_DEPOSIT_002("Valor do depósito abaixo do mínimo permitido"),
    ERR_DEPOSIT_100("Falha ao criar depósito"),
    ERR_WITHDRAWAL_001("Saque não encontrado"),
    ERR_WITHDRAWAL_002("Falha ao criar saque"),
    ERR_WITHDRAWAL_003("Saldo insuficiente"),
    ERR_WITHDRAWAL_004("Destino Inválido: "),

    ERR_BODY_001("Corpo da requisição inválido: "),
    ;


    String message;
}
