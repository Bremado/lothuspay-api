package com.lothuspay.wallet.service.ledger;

import com.lothuspay.wallet.dto.response.DtoResponse;
import com.lothuspay.wallet.dto.response.object.GetDeposit;
import com.lothuspay.wallet.dto.response.object.GetLedger;
import com.lothuspay.wallet.dto.response.object.GetWallet;
import com.lothuspay.wallet.dto.response.object.GetWithdraw;
import com.lothuspay.wallet.dto.response.status.DtoResponseStatus;
import com.lothuspay.wallet.model.deposit.Deposit;
import com.lothuspay.wallet.model.ledger.Ledger;
import com.lothuspay.wallet.model.ledger.type.LedgerType;
import com.lothuspay.wallet.model.withdraw.Withdraw;
import com.lothuspay.wallet.pojo.UserContext;
import com.lothuspay.wallet.repository.deposit.DepositRepository;
import com.lothuspay.wallet.repository.ledger.LedgerRepository;
import com.lothuspay.wallet.repository.wallet.WalletRepository;
import com.lothuspay.wallet.repository.withdraw.WithdrawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;
    private final DepositRepository depositRepository;
    private final WithdrawRepository withdrawRepository;

    public Mono<DtoResponse> statement(UserContext context, int page, int size) {
        return walletRepository.findByUserId(context.getUserId())
                .flatMapMany(wallet ->
                        ledgerRepository.findAllByWalletId(wallet.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created")))
                )
                .collectList()
                .flatMap(list -> {
                    var depositIds = list.stream()
                            .filter(l -> l.getType() == LedgerType.CREDIT)
                            .map(Ledger::getOriginId)
                            .toList();

                    var withdrawIds = list.stream()
                            .filter(l -> l.getType() == LedgerType.DEBIT)
                            .map(Ledger::getOriginId)
                            .toList();

                    Mono<List<Deposit>> depositsMono =
                            depositRepository.findAllByIdIn(depositIds).collectList();

                    Mono<List<Withdraw>> withdrawsMono =
                            withdrawRepository.findAllByIdIn(withdrawIds).collectList();

                    return Mono.zip(depositsMono, withdrawsMono)
                            .map(tuple -> {
                                var deposits = tuple.getT1();
                                var withdraws = tuple.getT2();

                                var ledgers = list.stream()
                                        .map(l -> {
                                            var ledger = new GetLedger(l);

                                            var obj = (l.getType() == LedgerType.CREDIT)
                                                    ? deposits.stream()
                                                    .filter(d -> d.getId().equals(l.getOriginId()))
                                                    .findFirst().orElse(null)
                                                    : withdraws.stream()
                                                    .filter(w -> w.getId().equals(l.getOriginId()))
                                                    .findFirst().orElse(null);

                                            if (ledger.getType() == LedgerType.CREDIT) {
                                                var deposit = (Deposit) obj;
                                                if (deposit != null) {
                                                    ledger.setTransaction(new GetDeposit(deposit));
                                                }
                                            } else if (ledger.getType() == LedgerType.DEBIT) {
                                                var withdraw = (Withdraw) obj;
                                                if (withdraw != null) {
                                                    ledger.setTransaction(new GetWithdraw(withdraw));
                                                }
                                            }

                                            return ledger;
                                        })
                                        .toList();

                                return DtoResponse.builder()
                                        .status(DtoResponseStatus.SUCCESS)
                                        .message(DtoResponseStatus.SUCCESS.getMessage())
                                        .data(ledgers)
                                        .build();
                            });
                })
                .switchIfEmpty(
                        Mono.just(
                                DtoResponse.builder()
                                        .status(DtoResponseStatus.ERR_WALLET_001)
                                        .message("Wallet not found.")
                                        .build()
                        )
                );
    }
}
