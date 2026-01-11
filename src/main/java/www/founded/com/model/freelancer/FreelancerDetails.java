package www.founded.com.model.freelancer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "freelancer")
public class FreelancerDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_details_id")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "fk_username_id")
	private Freelancer username;
	
	@ManyToOne
	@JoinColumn(name = "fk_profile_id")
	private Profile profile;
	
	@ManyToOne
	@JoinColumn(name = "fk_education_id")
	private Education education;
	
	@ManyToOne
	@JoinColumn(name = "fk_experience_id")
	private Experience experience;	
	
	@ManyToOne
	@JoinColumn(name = "fk_category_id")
	private UserCategory category;
	
	@ManyToOne
	@JoinColumn(name = "fk__userskill_id")
	private UserSkill userSkill;
	
	@ManyToOne
	@JoinColumn(name = "fk_user_language_id")
	private UserLanguage userLanguage;
}
