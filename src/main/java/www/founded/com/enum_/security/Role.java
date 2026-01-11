package www.founded.com.enum_.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Role {
	
	ADMIN(Set.of(Permission.READ_USER, Permission.DELETE_USER)), 
	CLIENT(Set.of(Permission.CREATE_GIG, Permission.READ_GIG, Permission.UPDATE_GIG, Permission.DELETE_GIG)),
	SELLER(Set.of(Permission.CREATE_GIG, Permission.READ_GIG, Permission.UPDATE_GIG, Permission.DELETE_GIG)),
	FREELANCER(Set.of(Permission.CREATE_GIG, Permission.READ_GIG, Permission.UPDATE_GIG, Permission.DELETE_GIG));
	
	private Set<Permission> permission;
	public Set<SimpleGrantedAuthority> getAuthorities(){
		Set<SimpleGrantedAuthority> grantedAuthorities = this.permission.stream()
			.map(permission -> new SimpleGrantedAuthority(permission.getDescripton()))
			.collect(Collectors.toSet());
		
		SimpleGrantedAuthority role = new SimpleGrantedAuthority("ROLE_" + this.name());
		grantedAuthorities.add(role);
		
		return grantedAuthorities;
	}
}
