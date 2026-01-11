package www.founded.com.service.notification;

import www.founded.com.model.chat_system.Recipient;
import www.founded.com.model.chat_system.Sender;

public interface NotificationService {
	void sendMessageNotification(Sender sender, Recipient recipient, String messageContent);
}
