package www.founded.com.repository.payment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.enum_.payment.WithdrawalStatus;
import www.founded.com.model.payment.Withdrawal;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    List<Withdrawal> findByWallet_Id(Long walletId);

    boolean existsByWallet_IdAndStatus(Long walletId, WithdrawalStatus status);
}
