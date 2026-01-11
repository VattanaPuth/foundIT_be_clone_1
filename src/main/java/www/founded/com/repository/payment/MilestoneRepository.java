package www.founded.com.repository.payment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.payment.Milestone;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
	List<Milestone> findByEscrow_Id(Long escrowId);

}
