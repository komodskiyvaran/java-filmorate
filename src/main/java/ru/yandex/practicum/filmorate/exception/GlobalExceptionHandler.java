package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public ErrorResponse handleValidationException(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return new ErrorResponse("Validation error", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleNotFoundException(NotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        System.out.println("=== HANDLING NOT FOUND EXCEPTION ==="); // ← добавить
        return new ErrorResponse("Not found", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicatedDataException.class)
    public ErrorResponse handleDuplicatedDataException(DuplicatedDataException ex) {
        log.warn("Duplicated data: {}", ex.getMessage());
        return new ErrorResponse("Duplicated data", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    public ErrorResponse handleThrowable(Throwable e) {
        System.out.println("=== HANDLING THROWABLE ==="); // ← добавить
        log.error("Internal server error", e);
        return new ErrorResponse("Internal server error", "An unexpected error has occurred.");
    }
}