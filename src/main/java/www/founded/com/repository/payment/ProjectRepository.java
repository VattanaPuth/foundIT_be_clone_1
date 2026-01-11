package www.founded.com.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.payment.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {}

