package br.com.lmello.secret_santa.infra;

import br.com.lmello.secret_santa.dto.ErrorMessageDTO;
import br.com.lmello.secret_santa.exception.InvalidAdminCodeException;
import br.com.lmello.secret_santa.exception.NotFoundException;
import br.com.lmello.secret_santa.exception.SecretSantaAlreadyStartedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DrawErrorHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException exception) {
        return ResponseEntity.ok(new ErrorMessageDTO(exception.getMessage()));
    }

    @ExceptionHandler(InvalidAdminCodeException.class)
    public ResponseEntity<?> handleWrongAdminCode(InvalidAdminCodeException exception) {
        return ResponseEntity.badRequest().body(new ErrorMessageDTO(exception.getMessage()));
    }

    @ExceptionHandler(SecretSantaAlreadyStartedException.class)
    public ResponseEntity<?> handleDrawAlreadyStarted(SecretSantaAlreadyStartedException exception) {
        return ResponseEntity.badRequest().body(new ErrorMessageDTO(exception.getMessage()));
    }
}
