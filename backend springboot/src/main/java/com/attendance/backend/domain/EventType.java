package com.attendance.backend.domain;

public enum EventType {
    CHECKIN_QR,
    MARK_MANUAL_PRESENT,
    MARK_MANUAL_LATE,
    MARK_MANUAL_ABSENT,
    MARK_EXCUSED,
    REVERT_FROM_EXCUSED,
    SESSION_OPENED,
    SESSION_CLOSED
}
