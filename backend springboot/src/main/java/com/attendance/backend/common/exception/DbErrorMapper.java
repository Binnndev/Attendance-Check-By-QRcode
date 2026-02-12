package com.attendance.backend.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DbErrorMapper {

    private static final String TRG_OWNER = "Cannot remove/reject OWNER directly. Transfer ownership first.";
    private static final String TRG_XOR = "Absence request must set exactly one of linked_session_id or requested_date.";
    private static final String TRG_FLOW = "Invalid status transition APPROVED -> PENDING";
    private static final String TRG_EVT_ACTOR = "SESSION_* events require actor_user_id";
    private static final String TRG_EVT_USER_ACTOR = "For SESSION_* events, set user_id = actor_user_id";
    private static final String TRG_IMMUT = "attendance_events are immutable (updates not allowed)";

    public ApiException map(DataIntegrityViolationException ex) {
        String root = rootMessage(ex);
        String low = root.toLowerCase(Locale.ROOT);

        // 409
        if (low.contains("uk_as_group_single_open"))
            return ApiException.conflict("SESSION_ALREADY_OPEN", "Group already has an OPEN session");
        if (low.contains("uk_groups_code"))
            return ApiException.conflict("GROUP_CODE_EXISTS", "Group code already exists");
        if (low.contains("uk_groups_join_code"))
            return ApiException.conflict("GROUP_JOIN_CODE_EXISTS", "Join code already exists");
        if (low.contains("uk_users_email_norm"))
            return ApiException.conflict("EMAIL_EXISTS", "Email already exists");
        if (low.contains("uk_users_user_code"))
            return ApiException.conflict("USER_CODE_EXISTS", "User code already exists");
        if (low.contains("uk_qt_token_hash"))
            return ApiException.conflict("QR_TOKEN_DUPLICATE_HASH", "QR token hash duplicated");

        // 422
        if (root.contains(TRG_OWNER))
            return ApiException.unprocessable("OWNER_CANNOT_BE_REMOVED", root);
        if (root.contains(TRG_XOR))
            return ApiException.unprocessable("ABSENCE_TARGET_XOR_REQUIRED", root);
        if (root.contains(TRG_FLOW))
            return ApiException.unprocessable("INVALID_STATUS_TRANSITION", root);
        if (root.contains(TRG_EVT_ACTOR))
            return ApiException.unprocessable("EVENT_INVALID_SESSION_ACTOR", root);
        if (root.contains(TRG_EVT_USER_ACTOR))
            return ApiException.unprocessable("EVENT_INVALID_SESSION_USER_ACTOR", root);
        if (root.contains(TRG_IMMUT))
            return ApiException.unprocessable("EVENTS_IMMUTABLE", root);

        return ApiException.badRequest("DATA_INTEGRITY_ERROR", root);
    }

    private String rootMessage(Throwable ex) {
        Throwable r = ex;
        while (r.getCause() != null) r = r.getCause();
        return r.getMessage() == null ? ex.getMessage() : r.getMessage();
    }
}
