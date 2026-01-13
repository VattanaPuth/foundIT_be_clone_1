package www.founded.com.service.client;

import www.founded.com.model.client.Client;

public interface ClientService {
	Client getById(Long id);
	Client getByUserId(Long userId);
}
