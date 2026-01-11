package www.founded.com.model.freelancer;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.freelancer.Experiencelevel;

@Data
@Entity
@Table(name = "freelancer_experience")
public class Experience {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_experience_id")
	private Long id;
	
	@Column(name = "year_of_experience")
	private Integer yearOfExperience;
	
	@Enumerated(EnumType.STRING)
	private Experiencelevel expLevel;
	
	@Column(name = "Title")
	private String title;
	
	@Column(name = "Company")
	private String company;
	
	@Column(name = "Start_Date")
	private LocalDate start_date;
	
	@Column(name = "End_Date")
	private LocalDate end_date;
	
	@Column(name = "Desccription")
	private String description;

}
