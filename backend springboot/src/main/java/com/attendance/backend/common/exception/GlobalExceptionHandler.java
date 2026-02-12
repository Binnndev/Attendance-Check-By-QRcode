package com.attendance.backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final DbErrorMapper dbErrorMapper;

    public GlobalExceptionHandler(DbErrorMapper dbErrorMapper) {
        this.dbErrorMapper = dbErrorMapper;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus()).body(body(
                ex.getStatus().value(), ex.getCode(), ex.getMessage(), req.getRequestURI()
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ApiException mapped = dbErrorMapper.map(ex);
        return ResponseEntity.status(mapped.getStatus()).body(body(
                mapped.getStatus().value(), mapped.getCode(), mapped.getMessage(), req.getRequestURI()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknown(Exception ex, HttpServletRequest req) {
        return ResponseEntity.internalServerError().body(body(
                500, "INTERNAL_SERVER_ERROR", ex.getMessage(), req.getRequestURI()
        ));
    }

    private Map<String, Object> body(int status, String code, String message, String path) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", Instant.now());
        m.put("status", status);
        m.put("code", code);
        m.put("message", message);
        m.put("path", path);
        m.put("traceId", UUID.randomUUID().toString());
        return m;
    }
}
