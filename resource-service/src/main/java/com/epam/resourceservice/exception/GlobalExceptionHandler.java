package com.epam.resourceservice.exception;

import com.epam.resourceservice.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

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


    @ExceptionHandler(SongServiceException.class)
    public ResponseEntity<ApiError> handleSongServiceError(
            SongServiceException ex
    ) {
        return error(
                HttpStatus.BAD_GATEWAY,
                ex.getMessage()
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