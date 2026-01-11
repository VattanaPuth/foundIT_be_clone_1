package www.founded.com.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class ResourceNotFoundException extends ApiException{

	public ResourceNotFoundException(String message, Long Id) {
		super(HttpStatus.NOT_FOUND, String.format("%s with id = %d Not Found", message, Id));
	}

}
