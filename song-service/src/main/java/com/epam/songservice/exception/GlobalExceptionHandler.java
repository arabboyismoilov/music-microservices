package com.epam.songservice.exception;

import com.epam.songservice.dto.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SongNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            SongNotFoundException exception
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateSongException.class)
    public ResponseEntity<ApiError> handleDuplicate(
            DuplicateSongException exception
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidIdException.class)
    public ResponseEntity<ApiError> handleInvalidId(
            InvalidIdException exception
    ) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleRequestValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> details = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = error.getDefaultMessage();
            String currentMessage = details.get(field);

            boolean currentIsRequired = currentMessage != null
                    && currentMessage.endsWith("is required");

            boolean newIsRequired = message != null
                    && message.endsWith("is required");

            if (currentMessage == null
                    || (currentIsRequired && !newIsRequired)) {
                details.put(field, message);
            }
        });

        return ResponseEntity.badRequest().body(
                new ApiError("Validation error", details, "400")
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        Map<String, String> details = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath()
                    .toString()
                    .replaceFirst("^.*\\.", "");

            details.putIfAbsent(field, violation.getMessage());
        });

        return ResponseEntity.badRequest().body(
                new ApiError("Validation error", details, "400")
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        String message;
        if ("id".equals(ex.getName())) {
            message = "Invalid value '" + ex.getValue()
                    + "' for ID. Must be a positive integer";
        } else {
            message = "Invalid value for parameter '" + ex.getName() + "'";
        }

        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(
            HttpMessageNotReadableException exception
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or contains invalid JSON"
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "A song with this ID already exists or violates a database constraint"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception
    ) {
        log.error("Unexpected error in Song Service", exception);

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred"
        );
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity.status(status).body(
                new ApiError(
                        message,
                        null,
                        String.valueOf(status.value())
                )
        );
    }
}