package br.com.lmello.secret_santa.infra;

import br.com.lmello.secret_santa.dto.ErrorMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class CommonErrorHandler {
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFond(NoResourceFoundException exception) {
        String errorMessage = exception.getMessage();
        String urlPath = errorMessage.substring(
                errorMessage.lastIndexOf(" ")+1,
                errorMessage.lastIndexOf("/")+1
        );

        String message = "URL not found: '"  + urlPath + "'";

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessageDTO(message));
    }
}
