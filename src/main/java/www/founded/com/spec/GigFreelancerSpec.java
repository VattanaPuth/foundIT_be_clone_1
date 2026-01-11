package www.founded.com.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import www.founded.com.model.freelancer.GigFreelancer;
import www.founded.com.utils.freelancer.GigFreelancerSpecFilter;

@Data
@SuppressWarnings("serial")
public class GigFreelancerSpec implements Specification<GigFreelancer>{
	private final GigFreelancerSpecFilter gigFreelancerSpecFilter;
	
	List<Predicate> predicates = new ArrayList<>();

	@Override
	public Predicate toPredicate(Root<GigFreelancer> gigFreelancer, CriteriaQuery<?> query, CriteriaBuilder cb) {
		
		if(gigFreelancerSpecFilter.getId() != null) {
			Predicate Id = gigFreelancer.get("id").in(gigFreelancerSpecFilter.getId());
			predicates.add(Id);
		}
		
		if(gigFreelancerSpecFilter.getName() != null) {
			Predicate Name = cb.like(cb.lower(gigFreelancer.get("name")), "%" + gigFreelancerSpecFilter.getName().toLowerCase() + "%");
			predicates.add(Name);
		}
		
		if(gigFreelancerSpecFilter.getShortBio() != null) {
			Predicate ShortBio = cb.like(cb.lower(gigFreelancer.get("shortBio")), "%" + gigFreelancerSpecFilter.getShortBio().toLowerCase() + "%");
			predicates.add(ShortBio);
		}
		
		if(gigFreelancerSpecFilter.getDescription() != null) {
			Predicate des = cb.like(cb.lower(gigFreelancer.get("description")), "%" + gigFreelancerSpecFilter.getDescription().toLowerCase() + "%");
			predicates.add(des);
		}
		
		if(gigFreelancerSpecFilter.getPrice() != null) {
			Predicate price = cb.greaterThanOrEqualTo(gigFreelancer.get("price"), gigFreelancerSpecFilter.getPrice());
			predicates.add(price);
		}
		
		return cb.and(predicates.toArray(Predicate[]::new));
	}

}


