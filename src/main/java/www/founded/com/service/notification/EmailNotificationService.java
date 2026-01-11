package www.founded.com.service.notification;

public interface EmailNotificationService {
	void sendEmail(String to, String subject, String text);
	void sendMessageNotificationToFreelancer(String freelancerEmail, String messageContents);
	void sendMessageNotificationToClient(String clientEmail, String messageContents);
}
