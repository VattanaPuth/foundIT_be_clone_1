package www.founded.com.dto.chat_system;

import lombok.Data;

@Data
public class ProposalActionDTO {
    private Long proposalId;
    private Long senderId;
    private Long gigId;
    private String messageType;
    private String status;
    private String action;
}
