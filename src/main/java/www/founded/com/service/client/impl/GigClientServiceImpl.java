package www.founded.com.service.client.impl;


import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.model.client.GigClient;
import www.founded.com.repository.client.GigClientRepository;
import www.founded.com.service.client.GigClientService;

@Service
@RequiredArgsConstructor
public class GigClientServiceImpl implements GigClientService{

    private final GigClientRepository gigRepo;

    // Set all gigs to public
    public List<GigClient> setAllPublic() {
        List<GigClient> allGigs = gigRepo.findAll();
        for (GigClient gig : allGigs) {
            gig.setPublic(true);  // Set each gig as public
        }
        return gigRepo.saveAll(allGigs);  // Save all updated gigs
    }

    // Get only public gigs
	@Override
	public Page<GigClient> getPublicGigs(Map<String, String> params, Pageable pageable) {
		return gigRepo.findByIsPublic(true, pageable);
	}

}
