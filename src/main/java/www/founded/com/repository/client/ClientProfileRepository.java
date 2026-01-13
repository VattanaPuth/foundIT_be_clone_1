package www.founded.com.repository.client;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.client.Client;
import www.founded.com.model.client.ClientProfile;

@Repository
public interface ClientProfileRepository extends JpaRepository<ClientProfile, Long> {
	Optional<ClientProfile> findByClient(Client client);
	Optional<ClientProfile> findByClient_IdAndIsPublicTrue(Long clientId);
}
