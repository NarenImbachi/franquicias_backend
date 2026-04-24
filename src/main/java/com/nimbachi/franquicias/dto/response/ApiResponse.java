package com.nimbachi.franquicias.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final String code;
    private final T data;
    private final Integer status;
    private final String path;

    // Constructor para respuestas exitosas
    private ApiResponse(boolean success, String message, String code, T data) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = data;
        this.status = null; 
        this.path = null;   
    }

    // Constructor para respuestas de error
    private ApiResponse(boolean success, String message, String code, Integer status, String path) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = null; 
        this.status = status;
        this.path = path;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, "SUCCESS", data);
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operación exitosa.", "SUCCESS", data);
    }

    public static <T> ApiResponse<T> error(String message, String code, HttpStatus status, String path) {
        return new ApiResponse<>(false, message, code, status.value(), path);
    }
}
