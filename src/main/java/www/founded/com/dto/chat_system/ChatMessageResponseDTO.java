package www.founded.com.dto.chat_system;

import java.sql.Time;
import java.time.DayOfWeek;

import jakarta.persistence.Lob;
import lombok.Data;
import www.founded.com.model.chat_system.Recipient;


@Data
public class ChatMessageResponseDTO {
	private String senderName;
	private String recipientName;
	private String contents;
	private String fileName;
	private String fileType;
	@Lob
	private byte[] fileData;
	private DayOfWeek day;
	private Time time;
}
