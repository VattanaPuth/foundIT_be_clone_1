package www.founded.com.repository.chat_system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.chat_system.Sender;

@Repository
public interface SenderRepository extends JpaRepository<Sender, Long>{

}
