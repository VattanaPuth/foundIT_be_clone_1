package www.founded.com.model.freelancer;

import java.time.LocalDate;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import www.founded.com.enum_.freelancer.Degree;

@Data
@Entity
@Table(name = "freelancer_education")
public class Education {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_education_id")
	private Long id;
	
	@Column(name = "university")
	private String university;
	
	@Column(name = "degree")
	private Degree degree;
	
	@Column(name = "field")
	private String field;
	
	@Column(name = "start_date")
	private LocalDate start_date;
	
	@Column(name = "end_date")
	private LocalDate end_date;
	
	@Lob                              
	@Basic(fetch = FetchType.LAZY)
    @Column(name = "certificate", nullable = true)
	private byte[] img;
}
