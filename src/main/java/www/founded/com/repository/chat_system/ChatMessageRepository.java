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
    @Query("SELECT m FROM Message m WHERE ((m.senderId.user = :sender AND m.recipientId.user = :recipient) " +
            "OR (m.senderId.user = :recipient AND m.recipientId.user = :sender)) " +
            "AND m.senderId.user.id <> m.recipientId.user.id ORDER BY m.time ASC")
    List<Message> findMessagesBetween(@Param("sender") UserRegister sender, @Param("recipient") UserRegister recipient);

    @Query("SELECT DISTINCT m FROM Message m WHERE (m.senderId.user.id = :userId OR m.recipientId.user.id = :userId) " +
           "AND m.senderId.user.id <> m.recipientId.user.id ORDER BY m.time DESC")
    List<Message> findConversationsByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE ((m.senderId.user.id = :userId AND m.recipientId.user.id = :otherUserId) " +
           "OR (m.senderId.user.id = :otherUserId AND m.recipientId.user.id = :userId)) " +
           "AND m.senderId.user.id <> m.recipientId.user.id ORDER BY m.time ASC")
    List<Message> findMessagesBetweenUsers(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);
}
