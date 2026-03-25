package com.lothuspay.wallet.model.withdraw.destionation.subtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WithdrawDestinationSubType {

    CNPJ("CNPJ"),
    CPF("CPF"),
    EMAIL("EMAIL"),
    PHONE("TELEFONE"),
    RANDOM_KEY("CHAVE_ALEATORIA");

    String value;

    public static WithdrawDestinationSubType fromString(String string) {
        for (WithdrawDestinationSubType type : WithdrawDestinationSubType.values()) {
            if (type.name().equalsIgnoreCase(string)) {
                return type;
            }
        }
        return null;
    }

}
