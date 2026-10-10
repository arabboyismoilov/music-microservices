package com.epam.resourceservice.exception;

import com.epam.resourceservice.dto.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<ApiError> handleInvalidFileFormat(
            InvalidFileFormatException ex
    ) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex
    ) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidResourceIdException.class)
    public ResponseEntity<ApiError> handleInvalidId(
            InvalidResourceIdException ex
    ) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleInvalidParameter(
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

    @ExceptionHandler(SongServiceException.class)
    public ResponseEntity<ApiError> handleSongServiceError(
            SongServiceException ex
    ) {
        log.error("Song service error: {}", ex.getMessage(), ex);
        return error(
                HttpStatus.BAD_GATEWAY,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(
            MissingServletRequestParameterException ex
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Required request parameter '" + ex.getParameterName()
                        + "' is missing"
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage(
            HttpMessageNotReadableException ex
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or invalid"
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex
    ) {
        return error(
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method '" + ex.getMethod() + "' is not supported"
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex
    ) {
        return error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content type is not supported"
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(
            NoResourceFoundException ex
    ) {
        return error(
                HttpStatus.NOT_FOUND,
                "Requested endpoint was not found"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception ex
    ) {
        log.error("Unexpected error in Resource Service", ex);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred"
        );
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String message
    ) {
        ApiError body = new ApiError(
                message,
                null,
                String.valueOf(status.value())
        );

        return ResponseEntity.status(status).body(body);
    }
}