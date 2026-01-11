package www.founded.com.repository.client;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import www.founded.com.model.client.GigClient;

@Repository
public interface GigClientRepository extends JpaRepository<GigClient, Long> {

    // Find gigs that are public
    Page<GigClient> findByIsPublic(boolean isPublic, Pageable pageable);
}
