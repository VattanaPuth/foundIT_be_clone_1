package www.founded.com.repository.freelancer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.freelancer.UserSkill;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

}