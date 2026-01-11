package www.founded.com.model.freelancer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.freelancer.Proficiency;

@Data
@Entity
@Table(name = "freelancer_user_language")
public class UserLanguage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_user_language_id")
	private Long id;
	private String userLanguage;
	
	@Enumerated(EnumType.STRING)
	private Proficiency proficiencyLanguage;
}
