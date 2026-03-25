package com.lothuspay.payments.service.deposit;

import com.lothuspay.events.dto.deposit.DepositCompleted;
import com.lothuspay.events.dto.deposit.DepositCreated;
import com.lothuspay.events.dto.email.EmailSend;
import com.lothuspay.payments.dto.request.PostCreateDeposit;
import com.lothuspay.payments.dto.request.callback.SubadquirerCallbackDto;
import com.lothuspay.payments.dto.response.DtoResponse;
import com.lothuspay.payments.dto.response.object.general.GetDeposit;
import com.lothuspay.payments.dto.response.status.DtoResponseStatus;
import com.lothuspay.payments.event.publisher.EventPublisher;
import com.lothuspay.payments.integration.subadquirer.SubadquirerIntegration;
import com.lothuspay.payments.integration.subadquirer.dto.SubadquirerDepositRequestDto;
import com.lothuspay.payments.model.deposit.DepositRequest;
import com.lothuspay.payments.model.deposit.status.DepositRequestStatus;
import com.lothuspay.payments.model.notification.Notification;
import com.lothuspay.payments.model.notification.payload.NotificationPayload;
import com.lothuspay.payments.model.notification.status.NotificationStatus;
import com.lothuspay.payments.model.notification.type.NotificationType;
import com.lothuspay.payments.pojo.UserContext;
import com.lothuspay.payments.repository.deposit.DepositRepository;
import com.lothuspay.payments.integration.wallet.WalletIntegration;
import com.lothuspay.payments.service.config.ConfigService;
import com.lothuspay.payments.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final EventPublisher publisher;
    private final ConfigService configService;
    private final DepositRepository depositRepository;
    private final WalletIntegration walletIntegration;
    private final SubadquirerIntegration subadquirerIntegration;
    private final NotificationService notificationService;

    public Mono<DtoResponse> create(UserContext context, PostCreateDeposit dto) {
        try {
            var config = configService.config();
            var wallet = walletIntegration.getWallet(context.getUserId());

            if (dto.getPayer() == null) {
                return Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_BODY_001)
                        .message(DtoResponseStatus.ERR_BODY_001.getMessage() + " - Missing payer information")
                        .build());
            }

            if (dto.getPayer().getName() == null || dto.getPayer().getName().isEmpty()) {
                return Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_BODY_001)
                        .message(DtoResponseStatus.ERR_BODY_001.getMessage() + " - Payer name is required")
                        .build());
            }

            if (dto.getPayer().getDocument() == null || dto.getPayer().getDocument().isEmpty()) {
                return Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_BODY_001)
                        .message(DtoResponseStatus.ERR_BODY_001.getMessage() + " - Payer document is required")
                        .build());
            }

            if (dto.getPayer().getEmail() == null || dto.getPayer().getEmail().isEmpty()) {
                return Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_BODY_001)
                        .message(DtoResponseStatus.ERR_BODY_001.getMessage() + " - Payer email is required")
                        .build());
            }

            return Mono.zip(config, wallet).flatMap((zip) -> {
                var cfg = zip.getT1();

                if (!cfg.isAllowDeposits()) {
                    return Mono.just(DtoResponse.builder()
                            .status(DtoResponseStatus.ERR_DEPOSIT_000)
                            .message(DtoResponseStatus.ERR_DEPOSIT_000.getMessage())
                            .build());
                }

                var w = zip.getT2();

                var depositRequest = dto.toDepositRequest(w.getWalletId());

                var fee = depositRequest.calculateFee(context.getSegment(), w.getTax());

                depositRequest.setFee(fee);
                depositRequest.setTotal(depositRequest.getSubTotal().subtract(fee));

                if (depositRequest.getSubTotal().compareTo(BigDecimal.valueOf(cfg.getMinDepositAmount())) < 0) {
                    return Mono.just(DtoResponse.builder()
                            .status(DtoResponseStatus.ERR_DEPOSIT_002)
                            .message("Deposit amount is below the minimum allowed (R$" + cfg.getMinDepositAmount() + ")")
                            .build());
                }

                var request = SubadquirerDepositRequestDto.builder()
                        .amount(depositRequest.getSubTotal())
                        .payerName(depositRequest.getPayer().getName())
                        .payerDocument(depositRequest.getPayer().getDocument())
                        .description(depositRequest.getDescription())
                        .transactionId(depositRequest.getId())
                        .projectWebhook("https://api.lothuspay.com/v1/payments/callback/deposit/" + depositRequest.getWalletId() + "/" + depositRequest.getId())
                        .build();

                return subadquirerIntegration.createDeposit(context.getSegment(), request)
                        .flatMap(response -> {
                            if (response.getData().getCopyPaste() == null) {
                                return Mono.just(DtoResponse.builder()
                                        .status(DtoResponseStatus.ERR_DEPOSIT_100)
                                        .message("Failed to create deposit with bank")
                                        .build());
                            }

                            depositRequest.setBrcode(response.getData().getCopyPaste());

                            return depositRepository.save(depositRequest)
                                    .map(savedDeposit -> {
                                        var payload = new DepositCreated(
                                                savedDeposit.getId(),
                                                savedDeposit.getWalletId(),
                                                savedDeposit.getReferenceId(),
                                                savedDeposit.getDescription(),
                                                savedDeposit.getMethod().name(),
                                                savedDeposit.getSubTotal(),
                                                savedDeposit.getFee(),
                                                savedDeposit.getTotal(),
                                                savedDeposit.getBrcode(),
                                                savedDeposit.getWebhook(),
                                                savedDeposit.getStatus().name(),
                                                savedDeposit.getCreated(),
                                                savedDeposit.getUpdated()
                                        );

                                        publisher.publish("payment.deposit.created", "DEPOSIT_CREATED", payload);

                                        return DtoResponse.builder()
                                                .status(DtoResponseStatus.SUCCESS)
                                                .data(new GetDeposit(savedDeposit))
                                                .build();
                                    }).switchIfEmpty(Mono.just(
                                            DtoResponse.builder()
                                                    .status(DtoResponseStatus.ERR_DEPOSIT_100)
                                                    .message("Failed to save deposit")
                                                    .build()
                                    ));
                        })
                        .doOnError(throwable -> {
                            System.out.println(throwable.getMessage());
                            throwable.printStackTrace();
                        })
                        .switchIfEmpty(Mono.just(DtoResponse.builder()
                                        .status(DtoResponseStatus.ERR_DEPOSIT_100)
                                        .message("Failed to create deposit with bank")
                                .build()));
            });
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.just(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_BODY_001)
                    .message(DtoResponseStatus.ERR_BODY_001.getMessage())
                    .build());
        }
    }
    public Mono<DtoResponse> callback(String walletId, String depositId, SubadquirerCallbackDto dto) {
        var deposit = depositRepository.findById(depositId);
        return deposit.flatMap(d -> {
            if (!d.getWalletId().equals(walletId)) {
                return Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_DEPOSIT_001)
                                .message("Deposit not found")
                                .build()
                );
            }

            var status = dto.getStatus().equalsIgnoreCase("COMPLETO") ? DepositRequestStatus.SUCCESS :
                    dto.getStatus().equalsIgnoreCase("PENDENTE") ? DepositRequestStatus.PENDING : DepositRequestStatus.FAILED;

            d.setStatus(status);
            d.setUpdated(LocalDateTime.now());

            var payload = new DepositCompleted(depositId, walletId, status.name(), d.getUpdated());

            publisher.publish("payment.deposit.completed", "DEPOSIT_PAID", payload);

            return depositRepository.save(d)
                    .flatMap(savedDeposit -> notificationService.sentNotification(
                            new Notification(
                                    UUID.randomUUID().toString(),
                                    NotificationType.CREDIT,
                                    depositId,
                                    d.getWebhook(),
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
                                    .data(new GetDeposit(savedDeposit))
                                    .build()
                    )

                    );
        }).switchIfEmpty(Mono.just(
                DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_DEPOSIT_001)
                        .message("Deposit not found")
                        .build()
        ));
    }

    public Mono<Void> expireDeposit(DepositRequest deposit) {
        deposit.setStatus(DepositRequestStatus.FAILED);
        deposit.setUpdated(LocalDateTime.now());

        var payload = new DepositCompleted(
                deposit.getId(),
                deposit.getWalletId(),
                deposit.getStatus().name(),
                deposit.getUpdated()
        );
        publisher.publish("payment.deposit.completed", "DEPOSIT_EXPIRED", payload);

        return depositRepository.save(deposit).then();
    }

    public Flux<DepositRequest> expiredDeposits() {
        var LocalDateTimeNow = LocalDateTime.now().minusHours(24);
        return depositRepository.findByStatusAndCreatedBefore(DepositRequestStatus.PENDING, LocalDateTimeNow);
    }

}

