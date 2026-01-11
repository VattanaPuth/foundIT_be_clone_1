package www.founded.com.repository.chat_system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import www.founded.com.model.chat_system.Message;
import www.founded.com.model.register.UserRegister;

@Repository
public interface ChatMessageRepository extends JpaRepository<Message, Long>{
    @Query("SELECT m FROM Message m WHERE (m.senderId.user = :sender AND m.recipientId.user = :recipient) " +
            "OR (m.senderId.user = :recipient AND m.recipientId.user = :sender) ORDER BY m.time ASC")
    List<Message> findMessagesBetween(@Param("sender") UserRegister sender, @Param("recipient") UserRegister recipient);
}
