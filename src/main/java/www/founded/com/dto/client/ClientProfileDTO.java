package www.founded.com.dto.client;

import java.util.List;

import lombok.Data;

@Data
public class ClientProfileDTO {
	private Long id;
	private String avatarUrl;
	private String fullName;
	private String titleRole;
	private String location;
	private Boolean allowMessages;
	private String shortBio;
	private List<String> valuesWhenHiring;
	private List<String> industries;
	private List<String> preferredWorkStyles;
	private List<String> hireCategories;
	private String fixedProjectMedian;
	private String hourlyMedian;
	private String contractLengthMedian;
	private String website;
	private String linkedin;
	private String xTwitter;
	private Boolean isPublic;
}
