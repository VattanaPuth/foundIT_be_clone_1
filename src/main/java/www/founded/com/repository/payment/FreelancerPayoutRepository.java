package www.founded.com.repository.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.payment.aba.FreelancerPayout;

@Repository
public interface FreelancerPayoutRepository extends JpaRepository<FreelancerPayout, Long> {
    Optional<FreelancerPayout> findByFreelancer_Id(Long freelancerId);
}
