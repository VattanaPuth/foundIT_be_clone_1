package www.founded.com.service.freelancer.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.exception.ResourceNotFoundException;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.freelancer.GigFreelancer;
import www.founded.com.repository.freelancer.FreelancerRepository;
import www.founded.com.service.freelancer.FreelancerService;

@Service
@RequiredArgsConstructor
public class FreelancerServiceImpl implements FreelancerService {

    private final FreelancerRepository freelancerRepo;

    @Override
    public Freelancer getById(Long id) {
        return freelancerRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Freelancer Not Found", id));
    }

	@Override
	public Freelancer getByName(String name) {
		return freelancerRepo.findByName(name);
	}
}
