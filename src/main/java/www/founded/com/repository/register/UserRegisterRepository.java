package www.founded.com.repository.register;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import www.founded.com.model.register.UserRegister;

public interface UserRegisterRepository extends JpaRepository<UserRegister, Long>{
	UserRegister findByUsername(String name);
	Optional<UserRegister> findByEmail(String email);
	
	// Find user by username or email
	@Query("SELECT u FROM UserRegister u WHERE u.username = :identifier OR u.email = :identifier")
	Optional<UserRegister> findByUsernameOrEmail(@Param("identifier") String identifier);
}
