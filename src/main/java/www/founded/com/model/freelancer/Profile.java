package www.founded.com.model.freelancer;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.freelancer.Experiencelevel;

@Data
@Entity
@Table(name = "freelancer_profile")
public class Profile {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_profile_id")
	private Long id;
	
	@Column(name = "Description")
	private String description;
	
	@Column(name = "Biology")
	private String bio;
	
	@Column(name = "Experience")
	private Experiencelevel experience_level;
	
	@Column(name = "Create_At")
	private LocalDate create_at;
	
	@Column(name = "Update_At")
	private LocalDate update_at;
}
