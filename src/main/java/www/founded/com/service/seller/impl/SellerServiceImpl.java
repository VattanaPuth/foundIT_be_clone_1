package www.founded.com.service.seller.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import www.founded.com.exception.ResourceNotFoundException;
import www.founded.com.model.seller.Seller;
import www.founded.com.repository.seller.SellerRepository;
import www.founded.com.service.seller.SellerService;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
	
	private final SellerRepository sellerRepo;

	@Override
	public Seller getById(Long id) {
		return sellerRepo.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Seller Not Found", id));
	}

	@Override
	public Seller getByName(String name) {
		return sellerRepo.findByName(name);
	}

}
