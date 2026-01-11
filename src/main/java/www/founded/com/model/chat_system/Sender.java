package www.founded.com.model.chat_system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.security.Role;
import www.founded.com.model.register.UserRegister;

@Entity
@Table(name = "sender")
@Data
public class Sender {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_sender_id")
	private Long id;
	
	@ManyToOne
    @JoinColumn(name = "user_id")  
    private UserRegister user; 
	
	private String senderName;
	
	@Enumerated(EnumType.STRING)
    private Role role;
}
