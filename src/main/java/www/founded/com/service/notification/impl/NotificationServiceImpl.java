package www.founded.com.service.notification.impl;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.model.chat_system.Recipient;
import www.founded.com.model.chat_system.Sender;
import www.founded.com.service.notification.EmailNotificationService;
import www.founded.com.service.notification.NotificationService;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
	 private final EmailNotificationService emailNotificationService;

	@Transactional
	@Override
	public void sendMessageNotification(Sender sender, Recipient recipient, String messageContent) {
        String subject = "New Message from " + sender.getSenderName();
        String body = sender.getSenderName() + " sent you a message: " + messageContent;

        // Example: send email notification to recipient
        emailNotificationService.sendEmail(recipient.getEmail(), subject, body);
    }

	
}
