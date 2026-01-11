package www.founded.com.service.client;


import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import www.founded.com.model.client.GigClient;

public interface GigClientService {
	List<GigClient> setAllPublic();
	Page<GigClient> getPublicGigs(Map<String, String> params, Pageable pageable);
}
