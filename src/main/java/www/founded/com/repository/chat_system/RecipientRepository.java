package www.founded.com.repository.chat_system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.chat_system.Recipient;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {

}
