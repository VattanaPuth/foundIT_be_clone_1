package www.founded.com.model.freelancer;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "freelancer_gig")
@Data
public class GigFreelancer{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_gig_id")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "username")
	private Freelancer freelancer;
	private String shortBio;
	private String description;
	private String ImageName;
	private String ImageType;
	
	@Lob
	private byte[] ImageData;
	
	@ManyToOne
	@JoinColumn(name = "fk_user_skill_id")
	@JsonProperty(namespace = "UserSkillId")
	private UserSkill userSkill;
	@Column(nullable = false)
	private boolean isPublic;
	private BigDecimal price;
}
