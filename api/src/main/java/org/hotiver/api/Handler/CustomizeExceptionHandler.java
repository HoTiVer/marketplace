package org.hotiver.api.Handler;

import org.hotiver.common.Exception.ErrorResource;
import org.hotiver.common.Exception.FieldErrorResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

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

}
