package www.founded.com.enum_.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Permission {
	
	//FREELANCER & SELLER
	CREATE_GIG("gig:create"),
	READ_GIG("gig:read"), //CLIENT
	UPDATE_GIG("gig:update"),
	DELETE_GIG("gig:delete"),
	
	//Admin
	READ_USER("user:read"),
	DELETE_USER("user:delete");
	
	private final String descripton;
}
