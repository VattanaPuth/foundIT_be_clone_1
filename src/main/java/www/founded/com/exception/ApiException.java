package www.founded.com.exception;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@SuppressWarnings("serial")
public class ApiException extends RuntimeException {
	private final HttpStatus status;
	private final String message;
}
