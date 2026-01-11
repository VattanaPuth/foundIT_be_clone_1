package www.founded.com.service.payment;

import java.math.BigDecimal;

import www.founded.com.model.payment.Withdrawal;

public interface WithdrawalService {
    Withdrawal requestWithdraw(Long walletId, BigDecimal amount);
    void processWithdrawalPayout(Long withdrawalId);
}
