package www.founded.com.model.client;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "client_profile")
public class ClientProfile {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_profile_id")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "fk_client_id", unique = true)
	private Client client;
	
	// Overview section
	@Column(name = "avatar_url", columnDefinition = "TEXT")
	private String avatarUrl;
	
	@Column(name = "full_name")
	private String fullName;
	
	@Column(name = "title_role")
	private String titleRole;
	
	@Column(name = "location")
	private String location;
	
	@Column(name = "allow_messages")
	private Boolean allowMessages = true;
	
	// About section
	@Column(name = "short_bio", columnDefinition = "TEXT")
	private String shortBio;
	
	@Column(name = "values_when_hiring", columnDefinition = "TEXT")
	private String valuesWhenHiring; // JSON array as string
	
	@Column(name = "industries", columnDefinition = "TEXT")
	private String industries; // JSON array as string
	
	@Column(name = "preferred_work_styles", columnDefinition = "TEXT")
	private String preferredWorkStyles; // JSON array as string
	
	// Hiring Highlights section
	@Column(name = "hire_categories", columnDefinition = "TEXT")
	private String hireCategories; // JSON array as string
	
	@Column(name = "fixed_project_median")
	private String fixedProjectMedian;
	
	@Column(name = "hourly_median")
	private String hourlyMedian;
	
	@Column(name = "contract_length_median")
	private String contractLengthMedian;
	
	// Links section
	@Column(name = "website")
	private String website;
	
	@Column(name = "linkedin")
	private String linkedin;
	
	@Column(name = "x_twitter")
	private String xTwitter;
	
	// Visibility
	@Column(name = "is_public")
	private Boolean isPublic = false;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
