package com.smarthr.backend.web.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiError handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, "NotFound", ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleConflict(ConflictException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "ConstraintViolation", "Violación de unicidad/constraint", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, "ValidationError", details, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "ValidationError", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "InvalidArgument", ex.getMessage(), req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "MalformedJson", "JSON mal formado o valor inválido", req);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiError handleGeneric(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "InternalError", "Error inesperado", req);
    }

    private ApiError error(HttpStatus status, String error, String message, HttpServletRequest req) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(req.getRequestURI())
                .build();
    }
}
