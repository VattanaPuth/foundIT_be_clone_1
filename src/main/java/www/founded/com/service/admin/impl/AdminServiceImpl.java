package www.founded.com.service.admin.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.admin.AdminPostDTO;
import www.founded.com.enum_.security.Role;
import www.founded.com.model.client.Client;
import www.founded.com.model.freelancer.Freelancer;
import www.founded.com.model.freelancer.GigFreelancer;
import www.founded.com.model.register.UserRegister;
import www.founded.com.model.seller.GigSeller;
import www.founded.com.model.seller.Order;
import www.founded.com.model.seller.Seller;
import www.founded.com.repository.client.ClientRepository;
import www.founded.com.repository.freelancer.FreelancerRepository;
import www.founded.com.repository.freelancer.GigFreelancerRepository;
import www.founded.com.repository.register.UserRegisterRepository;
import www.founded.com.repository.seller.GigSellerRepository;
import www.founded.com.repository.seller.OrderRepository;
import www.founded.com.repository.seller.SellerRepository;
import www.founded.com.service.admin.AdminService;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	private final UserRegisterRepository userRegisterRepo;

	private final ClientRepository clientRepo;
	private final FreelancerRepository freelancerRepo;
	private final SellerRepository sellerRepo;

	private final GigSellerRepository gigSellerRepo;
	private final GigFreelancerRepository gigFreelancerRepo;
	private final OrderRepository orderRepo;

	@Override
    public List<UserRegister> getAllUsers() {
        return userRegisterRepo.findAll();
    }

	@Override
	public List<AdminPostDTO> getAllPostsByUserRegisterId(Long userRegisterId) {
        UserRegister ur = userRegisterRepo.findById(userRegisterId)
                .orElseThrow(() -> new IllegalArgumentException("UserRegister not found"));

        Role role = ur.getRole(); // your enum Role

        List<AdminPostDTO> out = new ArrayList<>();

        if (role == Role.SELLER) {
            Seller seller = sellerRepo.findByUser_Id(userRegisterId)
                    .orElseThrow(() -> new IllegalStateException("Seller profile not found for this user"));

            List<GigSeller> gigs = gigSellerRepo.findBySellerId_Id(seller.getId());
            for (GigSeller g : gigs) {
                out.add(new AdminPostDTO(
                        "GIG_SELLER",
                        g.getId(),
                        g.getServiceType(),                  // title-like
                        g.isPublic() ? "PUBLIC" : "PRIVATE", // status-like
                        g.getPrice(),
                        null
                ));
            }
            return out;
        }

        if (role == Role.FREELANCER) {
            Freelancer freelancer = freelancerRepo.findByUser_Id(userRegisterId)
                    .orElseThrow(() -> new IllegalStateException("Freelancer profile not found for this user"));

            List<GigFreelancer> gigs = gigFreelancerRepo.findByFreelancer_Id(freelancer.getId());
            for (GigFreelancer g : gigs) {
                out.add(new AdminPostDTO(
                        "GIG_FREELANCER",
                        g.getId(),
                        g.getDescription(),     // change to your field name
                        "ACTIVE",
                        g.getPrice(),     // change to your field name
                        null
                ));
            }
            return out;
        }

        if (role == Role.CLIENT) {
            Client client = clientRepo.findByUser_Id(userRegisterId)
                    .orElseThrow(() -> new IllegalStateException("Client profile not found for this user"));

            List<Order> orders = orderRepo.findByClient_Id(client.getId());
            for (Order o : orders) {
                out.add(new AdminPostDTO(
                        "ORDER",
                        o.getOrderId(),
                        o.getProjectTitle(),
                        o.getStatus().name(),
                        o.getTotalAmount(),
                        o.getCreatedAt()
                ));
            }
            return out;
        }

        // If you also have ADMIN role inside UserRegister:
        return out;
    }


	@Override
    public void deleteUserRegister(Long userRegisterId) {
        UserRegister ur = userRegisterRepo.findById(userRegisterId)
                .orElseThrow(() -> new IllegalArgumentException("UserRegister not found"));

        Role role = ur.getRole();

        if (role == Role.SELLER) {
            Seller seller = sellerRepo.findByUser_Id(userRegisterId).orElse(null);
            if (seller != null) {
                // delete seller gigs first
                List<GigSeller> gigs = gigSellerRepo.findBySellerId_Id(seller.getId());
                gigSellerRepo.deleteAll(gigs);

                sellerRepo.delete(seller);
            }
        }

        if (role == Role.FREELANCER) {
            Freelancer freelancer = freelancerRepo.findByUser_Id(userRegisterId).orElse(null);
            if (freelancer != null) {
                List<GigFreelancer> gigs = gigFreelancerRepo.findByFreelancer_Id(freelancer.getId());
                gigFreelancerRepo.deleteAll(gigs);

                freelancerRepo.delete(freelancer);
            }
        }

        if (role == Role.CLIENT) {
            Client client = clientRepo.findByUser_Id(userRegisterId).orElse(null);
            if (client != null) {
                // if you keep orders, delete them (or soft delete) depending on your policy
                List<Order> orders = orderRepo.findByClient_Id(client.getId());
                orderRepo.deleteAll(orders);

                clientRepo.delete(client);
            }
        }

        // Finally delete core account row
        userRegisterRepo.delete(ur);
    }

	@Override
	public void deletePost(String postType, Long postId) {
        switch (postType) {
            case "GIG_SELLER" -> gigSellerRepo.deleteById(postId);
            case "GIG_FREELANCER" -> gigFreelancerRepo.deleteById(postId);
            case "ORDER" -> orderRepo.deleteById(postId);
            default -> throw new IllegalArgumentException("Unknown postType: " + postType);
        }
    }

}
