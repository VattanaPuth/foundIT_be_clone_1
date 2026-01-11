package www.founded.com.model.chat_system;

import java.sql.Time;
import java.time.DayOfWeek;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.service.chat_system.ChatMessageService;

@Entity
@Table(name = "message")
@Data
public class Message {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinTable(name = "fk_recipient_id")
	private Recipient recipientId;
	
	private String recipientName;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinTable(name = "fk_sender_id")
	private Sender senderId;
	
	private String senderName;
	
	@Column(nullable = false, length = 2000)
	private String contents;
	private DayOfWeek day;
	private Time time;
	private String fileName;
	private String fileType;
	@Lob
	private byte[] fileData;
	
	private Boolean read = false;
}
