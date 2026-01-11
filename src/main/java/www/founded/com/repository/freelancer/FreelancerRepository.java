package www.founded.com.repository.freelancer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.freelancer.Freelancer;

@Repository
public interface FreelancerRepository extends JpaRepository<Freelancer, Long> { 
	Freelancer findByName(String name);
	Optional<Freelancer> findByUser_Id(Long userRegisterId);
}

