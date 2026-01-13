package www.founded.com.service.client.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.exception.ResourceNotFoundException;
import www.founded.com.model.client.Client;
import www.founded.com.repository.client.ClientRepository;
import www.founded.com.service.client.ClientService;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
	
	private final ClientRepository clientRepo;

	@Override
	public Client getById(Long id) {
		return clientRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client Not Found ", id));
	}
	
	@Override
	public Client getByUserId(Long userId) {
		return clientRepo.findByUser_Id(userId).orElseThrow(() -> new ResourceNotFoundException("Client Not Found for User ", userId));
	}

}
