package www.founded.com.service.notification.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import www.founded.com.service.notification.EmailNotificationService;

@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

	private final JavaMailSender mailSender;
	
	@Transactional
	@Override
	public void sendEmail(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("your-email@gmail.com");  // Use your email or dynamic email
        mailSender.send(message);
    }

	@Transactional
	@Override
	public void sendMessageNotificationToFreelancer(String freelancerEmail, String messageContents) {
		String subject = "New Message from Client";
        String text = "You have received a new message from a client:\n\n" + messageContents;
        sendEmail(freelancerEmail, subject, text);
    }

	@Transactional
	@Override
	public void sendMessageNotificationToClient(String clientEmail, String messageContents) {
		String subject = "New Message from Freelancer";
        String text = "You have received a new message from a freelancer:\n\n" + messageContents;
        sendEmail(clientEmail, subject, text);
    }

}
