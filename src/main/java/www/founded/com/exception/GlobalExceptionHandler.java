package www.founded.com.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<?> handleApiException(ApiException e){
		ErrorResponse errRes = new ErrorResponse(e.getStatus(),e.getMessage());
		return ResponseEntity.status(e.getStatus()).body(errRes);
	}
}
