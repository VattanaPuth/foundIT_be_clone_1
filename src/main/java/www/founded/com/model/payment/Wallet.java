package www.founded.com.model.payment;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import www.founded.com.enum_.security.Role;

@Entity
@Table(name = "wallets",
       uniqueConstraints = @UniqueConstraint(name="uk_wallet_owner_role", columnNames={"owner_id","owner_role"}))
@Data
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Wallet owner: can be CLIENT or FREELANCER (you can also use userRegister.id)
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_role", nullable = false)
    private Role ownerRole;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal lockedBalance = BigDecimal.ZERO; // locked during withdrawal process

    @Column(length = 3, nullable = false)
    private String currency = "USD";

    @UpdateTimestamp
    private Instant updatedAt;
}
