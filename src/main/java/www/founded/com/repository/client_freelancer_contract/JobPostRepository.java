package www.founded.com.repository.client_freelancer_contract;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import www.founded.com.enum_.client_freelancer_contract.JobPostStatus;
import www.founded.com.model.client.Client;
import www.founded.com.model.payment.client_freelancer_contract.JobPost;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    List<JobPost> findByStatus(JobPostStatus status);
    List<JobPost> findByClientId(Client clientId);
}
