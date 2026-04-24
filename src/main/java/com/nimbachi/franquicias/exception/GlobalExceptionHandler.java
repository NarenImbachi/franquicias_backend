package com.nimbachi.franquicias.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.nimbachi.franquicias.dto.response.ApiResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private String getRequestPath(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación");

        log.warn("Error de validación en la solicitud {}: {}", getRequestPath(request), errorMessage);

        ApiResponse<Object> apiResponse = ApiResponse.error(
                errorMessage,
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST,
                getRequestPath(request));

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        log.warn("Recurso no encontrado en {}: {}", getRequestPath(request), ex.getMessage());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                ex.getMessage(),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND,
                getRequestPath(request));

        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {

        log.warn("Conflicto por recurso duplicado en {}: {}", getRequestPath(request), ex.getMessage());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                ex.getMessage(),
                "RESOURCE_DUPLICATE",
                HttpStatus.CONFLICT,
                getRequestPath(request));

        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Argumento ilegal o violación de regla de negocio en {}: {}", getRequestPath(request),
                ex.getMessage());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                ex.getMessage(),
                "BUSINESS_RULE_VIOLATION",
                HttpStatus.BAD_REQUEST,
                getRequestPath(request));

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("Error inesperado en {}: ", getRequestPath(request), ex);

        ApiResponse<Object> apiResponse = ApiResponse.error(
                "Ocurrió un error inesperado en el servidor.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                getRequestPath(request));

        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
