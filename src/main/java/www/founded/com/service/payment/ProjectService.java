package www.founded.com.service.payment;

import www.founded.com.dto.payment.ProjectCreateRequestDTO;
import www.founded.com.model.payment.Project;

public interface ProjectService {
	Project createProject(ProjectCreateRequestDTO req);
}
