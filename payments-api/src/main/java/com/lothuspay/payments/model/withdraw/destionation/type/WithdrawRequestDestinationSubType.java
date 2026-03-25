package com.lothuspay.payments.model.withdraw.destionation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WithdrawRequestDestinationSubType {

    CNPJ("CNPJ"),
    CPF("CPF"),
    EMAIL("EMAIL"),
    PHONE("TELEFONE"),
    RANDOM_KEY("CHAVE_ALEATORIA");

    String value;

    public static WithdrawRequestDestinationSubType fromString(String string) {
        for (WithdrawRequestDestinationSubType type : WithdrawRequestDestinationSubType.values()) {
            if (type.name().equalsIgnoreCase(string)) {
                return type;
            }
            if (type.getValue().equalsIgnoreCase(string)) {
                return type;
            }
        }
        return null;
    }

}
