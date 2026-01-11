package www.founded.com.model.payment;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import www.founded.com.enum_.payment.TransactionStatus;
import www.founded.com.enum_.payment.TransactionType;

@Entity
@Table(
  name = "transactions",
  uniqueConstraints = @UniqueConstraint(name="uk_txn_idempotency", columnNames="idempotency_key"),
  indexes = {
      @Index(name="idx_txn_escrow", columnList="escrow_id"),
      @Index(name="idx_txn_wallet", columnList="wallet_id"),
      @Index(name="idx_txn_payway", columnList="payway_txn_id")
  }
)
@Data
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="escrow_id")
    private Escrow escrow;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="wallet_id")
    private Wallet wallet;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private TransactionType type;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private TransactionStatus status;

    @Column(nullable=false, precision=18, scale=2)
    private BigDecimal amount;

    @Column(length=3, nullable=false)
    private String currency = "USD";

    @Column(name="payway_txn_id")
    private String paywayTxnId;

    @Column(name="payway_ref")
    private String paywayRef;

    @Column(name="idempotency_key", nullable=false, unique=true, length=120)
    private String idempotencyKey;

    @Column(columnDefinition="TEXT")
    private String rawResponse;

    @CreationTimestamp
    private Instant createdAt;
}


