package org.hotiver.api.Handler;

import jakarta.persistence.EntityNotFoundException;
import org.hotiver.common.Exception.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<FieldErrorResource> errorResources =
                e.getBindingResult().getFieldErrors().stream()
                        .map(
                                fieldError ->
                                        new FieldErrorResource(
                                                fieldError.getObjectName(),
                                                fieldError.getField(),
                                                fieldError.getCode(),
                                                fieldError.getDefaultMessage()))
                        .collect(Collectors.toList());

        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(new ErrorResource(errorResources));
    }

    @ExceptionHandler(NoAuthorizationException.class)
    public ResponseEntity<Object> handleNoAuthorizationException(
            NoAuthorizationException e,
            WebRequest request) {
        return ResponseEntity.status(UNAUTHORIZED).body(
            new HashMap<String, Object>() {{
                put("message", e.getMessage());
            }}
        );
    }

    // 409
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<Object> handleEntityAlreadyExistsException(
            EntityAlreadyExistsException e,
            WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new HashMap<String, Object>() {{
                    put("message", e.getMessage());
                }}
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(
            EntityNotFoundException e,
            WebRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new HashMap<String, Object>() {{
                    put("message", e.getMessage());
                }}
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(
            BadCredentialsException e,
            WebRequest request
    ) {
        return ResponseEntity.status(UNAUTHORIZED).body(
                new HashMap<String, Object>() {{
                    put("message", e.getMessage());
                }}
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException e,
            WebRequest request
    ) {
        return ResponseEntity.status(NOT_FOUND).body(
                new HashMap<String, Object>() {{
                    put("message", e.getMessage());
                }}
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(
            UserNotFoundException e,
            WebRequest request
    ) {
        return ResponseEntity.status(NOT_FOUND).body(
                new HashMap<String, Object>() {{
                    put("message", e.getMessage());
                }}
        );
    }

}
