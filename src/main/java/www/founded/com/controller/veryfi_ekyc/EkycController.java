package www.founded.com.controller.veryfi_ekyc;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.veryfi_ekyc.EkycRequestDTO;
import www.founded.com.dto.veryfi_ekyc.EkycResponseDTO;
import www.founded.com.service.veryfi_ekyc.impl.EkycServiceImpl;

@RestController
@RequestMapping("/ekyc")
@RequiredArgsConstructor
public class EkycController {

    private final EkycServiceImpl ekycService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public EkycResponseDTO verify(@RequestParam("file") MultipartFile file, @RequestParam("metadata") String metadataJson) throws Exception {
        EkycRequestDTO request = objectMapper.readValue(metadataJson, EkycRequestDTO.class);
        return ekycService.verifyIdDocument(file, request);
    }
}

