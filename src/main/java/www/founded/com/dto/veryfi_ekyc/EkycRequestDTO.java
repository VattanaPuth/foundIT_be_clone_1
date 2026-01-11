package www.founded.com.dto.veryfi_ekyc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import www.founded.com.enum_.veryfi_ekyc.IdDocType;

@Data
public class EkycRequestDTO {
	@NotBlank
    private String fullname;

    @NotBlank
    private String dob;

    @NotNull
    private IdDocType docType;
    
    private String sex;
    
    private String nationality;
}
