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
import www.founded.com.enum_.freelancer.SkillLevel;

@Data
@Entity
@Table(name = "freelancer_userskill")

public class UserSkill {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_userskill_id")
	private Long id;

	private String skill;
	@Enumerated(EnumType.STRING)
	@Column(name = "enum_skill_Level")
	private SkillLevel level;
}
