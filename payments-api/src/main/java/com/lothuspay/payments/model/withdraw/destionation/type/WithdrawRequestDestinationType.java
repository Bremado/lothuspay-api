package com.lothuspay.payments.model.withdraw.destionation.type;

import com.lothuspay.payments.model.withdraw.destionation.WithdrawRequestDestination;

public enum WithdrawRequestDestinationType {

    PIX,
    BANK_TRANSFER;

    public static WithdrawRequestDestinationType fromString(String type) {
        for (WithdrawRequestDestinationType destinationType : WithdrawRequestDestinationType.values()) {
            if (destinationType.name().equalsIgnoreCase(type)) {
                return destinationType;
            }
        }
        return null;
    }
}
