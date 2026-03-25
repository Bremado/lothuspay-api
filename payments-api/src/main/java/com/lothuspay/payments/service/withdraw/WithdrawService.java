package com.lothuspay.payments.service.withdraw;

import com.lothuspay.events.dto.withdraw.WithdrawCompleted;
import com.lothuspay.events.dto.withdraw.WithdrawCreated;
import com.lothuspay.payments.dto.request.PostCreateWithdraw;
import com.lothuspay.payments.dto.request.callback.SubadquirerCallbackDto;
import com.lothuspay.payments.dto.response.DtoResponse;
import com.lothuspay.payments.dto.response.status.DtoResponseStatus;
import com.lothuspay.payments.event.publisher.EventPublisher;
import com.lothuspay.payments.integration.subadquirer.SubadquirerIntegration;
import com.lothuspay.payments.integration.subadquirer.dto.SubadquirerWithdrawRequestDto;
import com.lothuspay.payments.integration.wallet.WalletIntegration;
import com.lothuspay.payments.model.deposit.status.DepositRequestStatus;
import com.lothuspay.payments.model.notification.Notification;
import com.lothuspay.payments.model.notification.payload.NotificationPayload;
import com.lothuspay.payments.model.notification.status.NotificationStatus;
import com.lothuspay.payments.model.notification.type.NotificationType;
import com.lothuspay.payments.model.withdraw.WithdrawRequest;
import com.lothuspay.payments.model.withdraw.status.WithdrawRequestStatus;
import com.lothuspay.payments.pojo.UserContext;
import com.lothuspay.payments.repository.withdraw.WithdrawRepository;
import com.lothuspay.payments.service.config.ConfigService;
import com.lothuspay.payments.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawService {

    private final EventPublisher publisher;
    private final ConfigService configService;
    private final WalletIntegration walletIntegration;
    private final WithdrawRepository withdrawRepository;
    private final SubadquirerIntegration subadquirerIntegration;
    private final NotificationService notificationService;

    public Mono<DtoResponse> create(UserContext context, PostCreateWithdraw dto) {
        var config = configService.config();
        var wallet = walletIntegration.getWallet(context.getUserId());

        System.out.println(dto.toString());

        if (dto.getDestination().getType() == null) {
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_WITHDRAWAL_004)
                    .message(DtoResponseStatus.ERR_WITHDRAWAL_004.getMessage() + " Destination type is required (PIX)")
                    .build());
        }

        if (dto.getDestination().getSubType() == null) {
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_WITHDRAWAL_004)
                    .message(DtoResponseStatus.ERR_WITHDRAWAL_004.getMessage() + " Destination subtype is required (e.g. CPF, CNPJ, EMAIL, PHONE, RANDOM_KEY)")
                    .build());
        }

        if (dto.getDestination().getDocumentType() == null) {
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_WITHDRAWAL_004)
                    .message(DtoResponseStatus.ERR_WITHDRAWAL_004.getMessage() + " Destination document type is required (CPF or CNPJ)")
                    .build());
        }

        if (dto.getReferenceId() == null || dto.getReferenceId().trim().equalsIgnoreCase("")) {
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_WITHDRAWAL_004)
                    .message(DtoResponseStatus.ERR_WITHDRAWAL_004.getMessage() + " Reference ID is required")
                    .build());
        }

        return Mono.zip(config, wallet).flatMap((zip) -> {
                    var cfg = zip.getT1();

                    if (!cfg.isAllowWithdrawals()) {
                        return Mono.just(DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_DEPOSIT_000)
                                .message(DtoResponseStatus.ERR_DEPOSIT_000.getMessage())
                                .build());
                    }

                    var w = zip.getT2();

                    var withdrawRequest = dto.toWithdrawRequest(w.getWalletId());

                    if (withdrawRequest.getSubTotal().compareTo(BigDecimal.valueOf(cfg.getMinWithdrawalAmount())) < 0) {
                        return Mono.just(DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_WITHDRAWAL_003)
                                .message("Minimum withdraw amount is " + cfg.getMinWithdrawalAmount())
                                .build());
                    }

                    if (withdrawRequest.getSubTotal().compareTo(w.getAvailable()) > 0) {
                        return Mono.just(DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_WITHDRAWAL_003)
                                .message("Insufficient wallet balance")
                                .build());
                    }

                    var fee = withdrawRequest.calculateFee(context.getSegment(), w.getTax());

                    var fixed = (context.getSegment().equals("NONE") || context.getSegment().equalsIgnoreCase("WHITELABEL") ? w.getTax().getPix().getWithdrawFixed() : w.getTax().getPix().getBlackWithdrawFixed());

                    withdrawRequest.setTotal(withdrawRequest.getSubTotal());
                    withdrawRequest.setFee(fee.add(fixed));

                    if (withdrawRequest.getTotal().compareTo(BigDecimal.valueOf(cfg.getMinWithdrawalAmount())) < 0) {
                        return Mono.just(DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_WITHDRAWAL_003)
                                .message("Total withdraw amount including fees must be at least " + cfg.getMinWithdrawalAmount())
                                .build());
                    }

                    if (withdrawRequest.getTotal().compareTo(w.getAvailable()) > 0) {
                        return Mono.just(DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_WITHDRAWAL_003)
                                .message("Insufficient wallet balance to cover total amount including fees")
                                .build());
                    }

                    var request = SubadquirerWithdrawRequestDto.builder()
                            .amount(Double.parseDouble(withdrawRequest.getTotal().toString()))
                            .pixKeyType(withdrawRequest.getDestination().getSubType().getValue())
                            .pixKey(withdrawRequest.getDestination().getDestination())
                            .description(withdrawRequest.getDescription())
                            .projectWebhook("https://api.lothuspay.com/v1/payments/callback/withdraw/" + withdrawRequest.getWalletId() + "/" + withdrawRequest.getId())
                            .build();

                    return withdrawRepository.save(withdrawRequest)
                            .flatMap(saved -> {
                                var payload = new WithdrawCreated(
                                        saved.getId(),
                                        saved.getWalletId(),
                                        saved.getReferenceId(),
                                        saved.getDescription(),
                                        new WithdrawCreated.Destination(
                                                saved.getDestination().getName(),
                                                saved.getDestination().getDocumentType().name(),
                                                saved.getDestination().getDocument(),
                                                saved.getDestination().getType().name(),
                                                saved.getDestination().getSubType().name(),
                                                saved.getDestination().getDestination()
                                        ),
                                        saved.getSubTotal(),
                                        saved.getFee(),
                                        saved.getTotal(),
                                        saved.getStatus().name(),
                                        saved.getCreated(),
                                        saved.getUpdated()
                                );

                                return subadquirerIntegration.createWithdraw(context.getSegment(), request)
                                        .map(response -> {
                                            publisher.publish("payment.withdraw.created", "WITHDRAW_CREATED", payload);
                                            var resDto = DtoResponse.builder()
                                                    .status(DtoResponseStatus.SUCCESS)
                                                    .message("Withdraw created successfully")
                                                    .data(payload)
                                                    .build();

                                            System.out.println(resDto.toString());
                                        
                                            return resDto; 
                                        })
                                        .switchIfEmpty(Mono.just(DtoResponse.builder()
                                                .status(DtoResponseStatus.ERR_WITHDRAWAL_002)
                                                .message("Failed to create withdraw with bank")
                                                .build()))
                                        .onErrorResume(ex ->
                                                withdrawRepository.deleteById(saved.getId())
                                                        .then(Mono.just(
                                                                DtoResponse.builder()
                                                                        .status(DtoResponseStatus.ERR_WITHDRAWAL_002)
                                                                        .message("Error creating withdraw with bank")
                                                                        .build()
                                                        ))
                                        );
                            }).switchIfEmpty(Mono.just(
                                    DtoResponse.builder()
                                            .status(DtoResponseStatus.ERR_WITHDRAWAL_002) // Set appropriate error status
                                            .message("Failed to save withdraw request")
                                            .build()
                            ));
                }).onErrorResume(
                        ex -> {
                            return Mono.just(DtoResponse.builder()
                                    .status(null)
                                    .message("Error creating withdraw: " + ex.getMessage())
                                    .build());
                        }
                )

                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_WITHDRAWAL_002) // Set appropriate error status
                        .message("Failed to create withdraw with bank")
                        .build()));

    }
    public Mono<DtoResponse> callback(String walletId, String withdrawId, SubadquirerCallbackDto dto) {
        var withdraw = withdrawRepository.findById(withdrawId);
        return withdraw.flatMap(d -> {
            if (!d.getWalletId().equals(walletId)) {
                return Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_WALLET_001)
                        .message("Wallet ID does not match withdraw record")
                        .build());
            }

            var status = dto.getStatus().equalsIgnoreCase("COMPLETO") ? WithdrawRequestStatus.APPROVED :
                    dto.getStatus().equalsIgnoreCase("PENDENTE") ? WithdrawRequestStatus.PENDING : WithdrawRequestStatus.REJECTED;

            d.setStatus(status);
            d.setUpdated(LocalDateTime.now());

            var payload = new WithdrawCompleted(
                    d.getId(),
                    d.getWalletId(),
                    d.getStatus().name(),
                    d.getUpdated()
            );
            publisher.publish("payment.withdraw.completed", "WITHDRAW_COMPLETED", payload);

            return withdrawRepository.save(d)
                    .flatMap(savedDeposit ->
                            notificationService.sentNotification(
                                    new Notification(
                                            UUID.randomUUID().toString(),
                                            NotificationType.DEBIT,
                                            withdrawId,
                                            savedDeposit.getWebhook(),
                                            NotificationPayload.builder()
                                                    .timestamp(System.currentTimeMillis())
                                                    .transactionId(savedDeposit.getId())
                                                    .status("COMPLETED")
                                                    .amount(Double.parseDouble(savedDeposit.getSubTotal().toString()))
                                                    .build(),
                                            NotificationStatus.SENDING
                                    )
                            ).thenReturn(
                                    DtoResponse.builder()
                                            .status(DtoResponseStatus.SUCCESS)
                                            .build()
                            )
                    );

        }).switchIfEmpty(Mono.just(
            DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_WITHDRAWAL_001)
                    .message("Withdraw not found")
                    .build(
        )));
    }

    public Mono<Void> expireWithdrawal(WithdrawRequest withdraw) {
        withdraw.setStatus(WithdrawRequestStatus.REJECTED);
        withdraw.setUpdated(LocalDateTime.now());

        var payload = new WithdrawCompleted(
                withdraw.getId(),
                withdraw.getWalletId(),
                withdraw.getStatus().name(),
                withdraw.getUpdated()
        );
        publisher.publish("payment.withdraw.completed", "WITHDRAW_EXPIRED", payload);

        return withdrawRepository.save(withdraw).then();
    }

    public Flux<WithdrawRequest> expiredWithdrawals() {
        var today = LocalDateTime.now().minusHours(24);
        return withdrawRepository.findByStatusAndCreatedBefore(WithdrawRequestStatus.PENDING, today);
    }
}
