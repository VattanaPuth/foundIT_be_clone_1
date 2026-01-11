package www.founded.com.service.freelancer.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.exception.ResourceNotFoundException;
import www.founded.com.model.freelancer.UserSkill;
import www.founded.com.repository.freelancer.UserSkillRepository;
import www.founded.com.service.freelancer.UserSkillService;

@Service
@RequiredArgsConstructor
public class UserSkillServiceImpl implements UserSkillService{
	public final UserSkillRepository usRepo;

	@Override
	public UserSkill getUserSkillById(Long id) {
		return usRepo.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("UserSkill", id));
	}

}
