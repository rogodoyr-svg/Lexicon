package com.lexicon.libro.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.lexicon.libro.dto.ApiResponse;
import com.lexicon.libro.dto.ErrorDetails;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(
            ApiException ex,
            WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(ex.getCode(), ex.getMessage());
        ApiResponse<?> response = new ApiResponse<>(false, errorDetails);

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(
            Exception ex,
            WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred: " + ex.getMessage()
        );
        ApiResponse<?> response = new ApiResponse<>(false, errorDetails);

        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
