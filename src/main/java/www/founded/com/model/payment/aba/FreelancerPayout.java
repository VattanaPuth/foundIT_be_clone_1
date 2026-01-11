package www.founded.com.model.payment.aba;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;
import www.founded.com.model.freelancer.Freelancer;

@Entity
@Data
public class FreelancerPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Freelancer freelancer;

    // Provided by ABA PayWay portal
    @Column(nullable = false)
    private String beneficiariesToken;

    private String bankName;
    private String accountLast4;
}

