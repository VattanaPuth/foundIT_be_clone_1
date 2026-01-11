package www.founded.com.service.freelancer;

import www.founded.com.model.freelancer.Freelancer;

public interface FreelancerService {
	Freelancer getById(Long id);
	Freelancer getByName(String name);
}
