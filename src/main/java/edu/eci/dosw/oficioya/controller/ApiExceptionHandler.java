package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.ApiError;
import edu.eci.dosw.oficioya.exception.CredencialesInvalidasException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return ApiError.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Error de validación en los datos ingresados o rol no permitido",
            request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(IllegalArgumentException ex, HttpServletRequest request) {
        return ApiError.of(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleCredencialesInvalidasException(CredencialesInvalidasException ex, HttpServletRequest request) {
        return ApiError.of(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI()
        );
    }
}
