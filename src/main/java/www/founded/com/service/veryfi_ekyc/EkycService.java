package www.founded.com.service.veryfi_ekyc;

import org.springframework.web.multipart.MultipartFile;

import www.founded.com.dto.veryfi_ekyc.EkycRequestDTO;
import www.founded.com.dto.veryfi_ekyc.EkycResponseDTO;

public interface EkycService {
	EkycResponseDTO verifyIdDocument(MultipartFile file, EkycRequestDTO request);
}
