package www.founded.com.repository.chat_system;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.chat_system.Recipient;
import www.founded.com.model.register.UserRegister;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {
	Optional<Recipient> findByUser(UserRegister user);
}


