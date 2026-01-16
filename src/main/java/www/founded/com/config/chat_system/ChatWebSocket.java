package www.founded.com.config.chat_system;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import www.founded.com.dto.chat_system.CallSignalDTO;
import www.founded.com.dto.chat_system.ChatMessageRequestDTO;
import www.founded.com.dto.chat_system.ChatMessageResponseDTO;
import www.founded.com.dto.chat_system.ProposalActionDTO;
import www.founded.com.dto.chat_system.WebSocketClientRequestDTO;
import www.founded.com.dto.chat_system.WebSocketServerResponseDTO;
import www.founded.com.service.chat_system.ChatMessageService;
import www.founded.com.utils.freelancer.chat_system.MockMultipartFile;

@RequiredArgsConstructor
public class ChatWebSocket extends TextWebSocket{
    private final ObjectMapper mapper;
    private final ChatMessageService chatService;
    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<String, Set<WebSocketSession>>();
    
    // userIdSession
    private String getUserId(WebSocketSession session) {
    	String query = session.getUri() != null 
    			? session.getUri().getQuery() : null; 
    	if(query != null) {
    		for(String part : query.split("&")) {
    			String[] key = part.split("=",2);
    			if(key.length == 2 && key[0].equals("userId")) {
    				return URLDecoder.decode(key[1], StandardCharsets.UTF_8);
    			}
    		}
    		
    	}
    	return "anonymous";
    }
    
    //connection
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        System.out.println("[DEBUG] afterConnectionEstablished: userId=" + userId + ", sessionId=" + session.getId());
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                    .add(session);
    }
    
    //connection closed
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String userId = getUserId(session);
		// get all the value pair in the map to remove after the connection is closed
		Set<WebSocketSession> set = userSessions.get(userId);// Set[value of userId(1), ...,value of userId(n)]
		if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                userSessions.remove(userId);
            }
		}
	}
	
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebSocketClientRequestDTO event = mapper.readValue(message.getPayload(), WebSocketClientRequestDTO.class);
        switch (event.getType()) {
            case "MESSAGE":
                handleSendMessage(session, event);
                break;
            case "CALL":
                handleCallSignal(session, event);
                break;
            case "proposal_action":
                handleProposalAction(session, event);
                break;
            default:
                sendError(session, "Unknown event type: " + event.getType());
        }
    }
	
	//handleSendMessage
    private void handleSendMessage(WebSocketSession session, WebSocketClientRequestDTO event) throws Exception {
        ChatMessageRequestDTO req = mapper.readValue(event.getPayload(), ChatMessageRequestDTO.class);

        String senderIdStr = getUserId(session);
        Long senderId;
        try {
            senderId = Long.parseLong(senderIdStr);
        } catch (NumberFormatException e) {
            sendError(session, "Invalid senderId: " + senderIdStr);
            return;
        }

        if (req.getRecipientName() == null) {
            sendError(session, "recipientId is required for direct chat");
            return;
        }

        Long recipientId;
        try {
            recipientId = Long.parseLong(req.getRecipientName());
        } catch (NumberFormatException e) {
            sendError(session, "Invalid recipientId: " + req.getRecipientName());
            return;
        }

        System.out.println("[DEBUG] handleSendMessage: senderId=" + senderId + ", recipientId=" + recipientId + ", contents='" + req.getContents() + "'");

        byte[] fileData = req.getFileData(); 
        String fileName = req.getFileName();
        String fileType = req.getFileType();

        MultipartFile file = null;
        if (fileData != null && fileData.length > 0) {
            file = new MockMultipartFile(fileName, fileName, fileType, fileData);
        }

        ChatMessageResponseDTO saved = chatService.sendMessage(senderId, recipientId, req, file);

        WebSocketServerResponseDTO<ChatMessageResponseDTO> serverEvent = new WebSocketServerResponseDTO<>("MESSAGE", saved);
        String json = mapper.writeValueAsString(serverEvent);

        sendToUser(String.valueOf(senderId), json);
        if (!recipientId.equals(senderId)) {
            sendToUser(String.valueOf(recipientId), json);
        }
    }

	//handleCallSignal
	private void handleCallSignal(WebSocketSession session, WebSocketClientRequestDTO event) throws Exception {
		//serialize data, from object into bytes
		// readValue == read data from the byte of payload or json of CallSignalDTO and convert into object ==> deserialize
        CallSignalDTO signal = mapper.readValue(event.getPayload(), CallSignalDTO.class);

        // trust server-side userId
        signal.setFromUserId(getUserId(session));

        WebSocketServerResponseDTO<CallSignalDTO> serverEvent = new WebSocketServerResponseDTO<>("CALL", signal);
        String json = mapper.writeValueAsString(serverEvent);

        // deliver to target user only
        if (signal.getToUserId() != null && !signal.getToUserId().isBlank()) {
            sendToUser(signal.getToUserId(), json);
        } else {
            sendError(session, "CALL missing toUserId");
        }
    }

	
	//SendtoUser
	private <T> void sendToUser(T userId, String json) throws IOException {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            System.err.println("No active sessions found for user: " + userId);
            return;
        }
        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                System.err.println("Error sending message to user " + userId + ": " + e.getMessage());
            }
        }
    }

	//SendError
	private void sendError(WebSocketSession session, String msg) throws IOException {
        WebSocketServerResponseDTO<String> error =
                new WebSocketServerResponseDTO<>("ERROR", msg);
        // sendMessage(WebSocketMessage<?> message);
        session.sendMessage(new TextMessage(mapper.writeValueAsString(error)));
        // TextMessage belong to WebSocketMessage
    }

	//handleProposalAction
	private void handleProposalAction(WebSocketSession session, WebSocketClientRequestDTO event) throws Exception {
		ProposalActionDTO action = mapper.readValue(event.getPayload(), ProposalActionDTO.class);

		String senderIdStr = getUserId(session);
		Long senderId;
		try {
			senderId = Long.parseLong(senderIdStr);
		} catch (NumberFormatException e) {
			sendError(session, "Invalid senderId: " + senderIdStr);
			return;
		}

		System.out.println("[DEBUG] handleProposalAction: senderId=" + senderId + ", proposalId=" + action.getProposalId() + ", action=" + action.getAction());

		chatService.handleProposalAction(action);

		// Broadcast the action result to both users
		WebSocketServerResponseDTO<ProposalActionDTO> serverEvent = new WebSocketServerResponseDTO<>("PROPOSAL_ACTION_RESULT", action);
		String json = mapper.writeValueAsString(serverEvent);

		sendToUser(String.valueOf(senderId), json);
		// Assuming gigId is the other user id, send to them too
		if (action.getGigId() != null) {
			sendToUser(action.getGigId(), json);
		}
	}


}
