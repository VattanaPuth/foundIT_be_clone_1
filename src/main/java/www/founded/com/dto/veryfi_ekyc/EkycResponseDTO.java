package www.founded.com.dto.veryfi_ekyc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EkycResponseDTO {
	private boolean success;
    private String reason;
    private String fullname;
    private String height;
    private String gender;
    private String dateOfBirth;
    private String documentNumber;
    private String createDate;
    private String expirationDate;
    private String address;
    private String nationality;
    private String docType;
}
