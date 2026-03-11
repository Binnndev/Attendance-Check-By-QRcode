package com.attendance.backend.common.exception;

import com.attendance.backend.common.db.DbErrorTranslator;
import com.attendance.backend.common.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final DbErrorTranslator dbErrorTranslator;

    public GlobalExceptionHandler(DbErrorTranslator dbErrorTranslator) {
        this.dbErrorTranslator = dbErrorTranslator;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApi(ApiException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus())
                .body(baseBody(ex.getStatus().value(), ex.getCode(), ex.getMessage(), req));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(400, "VALIDATION_ERROR", "Invalid request", req);
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, Object> body = baseBody(400, "VALIDATION_ERROR", ex.getMessage(), req);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(baseBody(400, "BAD_REQUEST", "Malformed JSON request", req));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(403).body(baseBody(403, "FORBIDDEN", "Access denied", req));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return ResponseEntity.status(401).body(baseBody(401, "UNAUTHORIZED", "Unauthorized", req));
    }

    @ExceptionHandler({DataIntegrityViolationException.class, DataAccessException.class})
    public ResponseEntity<?> handleDb(Exception ex, HttpServletRequest req) {
        log.error("Database error on {} {}", req.getMethod(), req.getRequestURI(), ex);
        ApiException api = dbErrorTranslator.translate(ex);
        return ResponseEntity.status(api.getStatus())
                .body(baseBody(api.getStatus().value(), api.getCode(), api.getMessage(), req));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity.status(500)
                .body(baseBody(500, "INTERNAL_ERROR", "Unexpected error", req));
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleUnsupportedMediaType(
            org.springframework.web.HttpMediaTypeNotSupportedException ex,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(415)
                .body(baseBody(415, "UNSUPPORTED_MEDIA_TYPE", "Content-Type must be application/json", req));
    }

    private Map<String, Object> baseBody(int status, String code, String message, HttpServletRequest req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("code", code);
        body.put("message", message);
        body.put("path", req.getRequestURI());

        String requestId = MDC.get(RequestIdFilter.MDC_KEY);
        if (requestId != null) body.put("requestId", requestId);

        return body;
    }
}