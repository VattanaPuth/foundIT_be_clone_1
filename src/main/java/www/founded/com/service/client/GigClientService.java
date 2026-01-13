package www.founded.com.service.client;


import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import www.founded.com.dto.client.GigClientDTO;
import www.founded.com.model.client.GigClient;

public interface GigClientService {
	List<GigClient> setAllPublic();
	GigClient publishGig(Long gigId, String userEmail);
	Page<GigClient> getPublicGigs(Map<String, String> params, Pageable pageable);
	GigClient createGig(GigClientDTO gigDTO, String userEmail);
	Page<GigClient> getGigsByUser(String userEmail, Pageable pageable);
	GigClient updateGig(Long gigId, GigClientDTO gigDTO, String userEmail);
	void deleteGig(Long gigId, String userEmail);
	int migrateClientIds();
}
