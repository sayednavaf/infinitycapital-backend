package com.infinitycapital.api.common;

import com.infinitycapital.api.market.MarketDataUnavailableException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MarketDataUnavailableException.class)
    public ResponseEntity<Map<String, String>> marketUnavailable(MarketDataUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", message(exception)));
    }

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> badRequest(Exception exception) {
        return ResponseEntity.badRequest().body(Map.of("message", message(exception)));
    }

    private String message(Exception exception) {
        return exception.getMessage() == null ? "The request could not be processed." : exception.getMessage();
    }
}
