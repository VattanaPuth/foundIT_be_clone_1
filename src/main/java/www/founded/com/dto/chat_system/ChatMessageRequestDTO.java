package www.founded.com.dto.chat_system;

import java.sql.Time;
import java.time.DayOfWeek;

import jakarta.persistence.Lob;
import lombok.Data;

@Data
public class ChatMessageRequestDTO {
	private String recipientName;
	private String senderName;
	private String contents;
	private String messageType = "text";
	private String fileName;
	private String fileType;
	@Lob
	private byte[] fileData;
	private DayOfWeek day;
	private Time time;
	
	private Long gigId;
}
