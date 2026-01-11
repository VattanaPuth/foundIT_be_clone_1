package www.founded.com.repository.freelancer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import www.founded.com.model.freelancer.GigFreelancer;

@Repository
public interface GigFreelancerRepository extends JpaRepository<GigFreelancer, Long>, JpaSpecificationExecutor<GigFreelancer>{
	Page<GigFreelancer> findByIsPublic(boolean isPublic, Pageable pageable);
	List<GigFreelancer> findByFreelancer_Id(Long freelancerId);
}
