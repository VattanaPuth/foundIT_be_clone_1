package www.founded.com.dto.freelancer;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GigFreelancerDTO {
	  
	  @JsonProperty("Name")       
	  private String name;
	  
	  @JsonProperty("ShortBio")   
	  private String shortBio;
	  
	  @JsonProperty("Description")
	  private String description;
	  
	  @JsonProperty("ImageProfile") 
	  private byte[] ImageData;
	  
	  @JsonProperty("UserSkillId")       
	  private Long userSkillId;
	  
	  @JsonProperty("Price")     
	  private BigDecimal price;
}
