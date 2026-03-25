package com.lothuspay.payments.model.withdraw.destionation.document;

public enum WithdrawRequestDocumentType {

    CPF,
    CNPJ;

    public static WithdrawRequestDocumentType fromString(String value) {
        for (WithdrawRequestDocumentType type : WithdrawRequestDocumentType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

}
