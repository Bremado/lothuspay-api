package com.lothuspay.wallet.listener;

import com.lothuspay.events.EventMessage;
import com.lothuspay.events.dto.deposit.DepositCompleted;
import com.lothuspay.events.dto.deposit.DepositCreated;
import com.lothuspay.events.dto.deposit.DepositUpdated;
import com.lothuspay.events.dto.withdraw.WithdrawCompleted;
import com.lothuspay.events.dto.withdraw.WithdrawCreated;
import com.lothuspay.wallet.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTopicListener {

    private final WalletService walletService;

    @KafkaListener(topics = "payment.deposit.created", groupId = "wallet-service")
    public void listenDepositCreated(EventMessage message) {
        var created = message.getPayloadAs(DepositCreated.class);
        walletService.createDeposit(created).subscribe();
    }

    @KafkaListener(topics = "payment.deposit.updated", groupId = "wallet-service")
    public void listenDepositUpdated(EventMessage message) {
        var updated = message.getPayloadAs(DepositUpdated.class);
        walletService.updateDeposit(updated).subscribe();
    }

    @KafkaListener(topics = "payment.deposit.completed", groupId = "wallet-service")
    public void listenDepositCompleted(EventMessage message) {
        var completed = message.getPayloadAs(DepositCompleted.class);
        log.info("Received DepositCompleted event: {}", completed.getId());
        walletService.completeDeposit(completed).subscribe();
    }

    @KafkaListener(topics = "payment.withdraw.created", groupId = "wallet-service")
    public void listenWithdrawCreated(EventMessage message) {
        var created = message.getPayloadAs(WithdrawCreated.class);
        walletService.createWithdraw(created).subscribe();
    }

    @KafkaListener(topics = "payment.withdraw.completed", groupId = "wallet-service")
    public void listenWithdrawCompleted(EventMessage message) {
        var completed = message.getPayloadAs(WithdrawCompleted.class);
        log.info("Received WithdrawCompleted event: {}", completed.getId());
        walletService.completeWithdraw(completed).subscribe();
    }
}
