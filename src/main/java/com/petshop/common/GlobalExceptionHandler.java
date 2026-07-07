package com.petshop.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_VALIDATION_MESSAGE = "Invalid request parameters";

    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        return Result.fail(401, "Please log in first");
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        return Result.fail(403, "Permission denied");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        return Result.fail(400, validationMessage(e.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        return Result.fail(400, validationMessage(e.getBindingResult()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.fail(400, fallbackMessage(e, DEFAULT_VALIDATION_MESSAGE));
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.warn("Business exception: {}", e.getMessage(), e);
        return Result.fail(fallbackMessage(e, "Request failed"));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("System exception", e);
        return Result.fail("System is busy, please try again later");
    }

    private String validationMessage(BindingResult bindingResult) {
        List<ObjectError> errors = bindingResult.getAllErrors();
        if (errors.isEmpty() || errors.get(0).getDefaultMessage() == null) {
            return DEFAULT_VALIDATION_MESSAGE;
        }
        return errors.get(0).getDefaultMessage();
    }

    private String fallbackMessage(Exception e, String fallback) {
        return e.getMessage() == null || e.getMessage().isBlank() ? fallback : e.getMessage();
    }
}
