package www.founded.com.utils.freelancer;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class GigFreelancerSpecFilter {
	private Long id;
	private String name;
	private String shortBio;
	private String description;
	private BigDecimal price;
}
