package edu.campusnest.web;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> badRequest(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("code","BAD_REQUEST","message",e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("code","VALIDATION_ERROR","message","Check the submitted fields."));}
 @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class) ResponseEntity<?> conflict(){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code","CONFLICT","message","The operation conflicts with existing records."));}
 @ExceptionHandler(java.util.NoSuchElementException.class) ResponseEntity<?> notFound(java.util.NoSuchElementException e){return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code","NOT_FOUND","message",e.getMessage()));}
 @ExceptionHandler(IllegalStateException.class) ResponseEntity<?> conflictState(IllegalStateException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code","CONFLICT","message",e.getMessage()));}
 @ExceptionHandler(org.springframework.security.core.AuthenticationException.class) ResponseEntity<?> authFailure(){return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code","INVALID_CREDENTIALS","message","Email or password is incorrect, or the account is unavailable."));}
 @ExceptionHandler(jakarta.validation.ConstraintViolationException.class) ResponseEntity<?> constraint(){return ResponseEntity.badRequest().body(Map.of("code","VALIDATION_ERROR","message","Check the request parameters."));}
}
