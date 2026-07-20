# UniAttend v0.9.0 — Portfolio Release

First public portfolio release of **UniAttend**, a QR-based classroom attendance platform with a lecturer web portal, a student Android app, and a Spring Boot backend.

## Included

### Lecturer Web Portal

- Lecturer dashboard and teaching-class overview
- Class creation and academic schedule configuration
- Student and membership management
- Dynamic QR attendance sessions
- Session history and attendance review
- Manual attendance correction
- Absence-request review
- Suspicious activity and shared-device monitoring views
- Attendance export support

### Student Android App

- Authentication and personal profile
- Class list and weekly schedule
- Join-class QR/code flow
- CameraX and ML Kit QR scanning
- QR check-in with stable device ID
- Check-in result screen
- Personal attendance history
- Notifications and unread state

### Spring Boot Backend

- Spring Boot 3.5.10 with Java 17
- Versioned REST API under `/api/v1`
- JWT authentication and persisted refresh sessions
- Role-aware class and membership workflows
- Attendance-session lifecycle management
- Rotating QR token generation and validation
- `PRESENT`, `LATE`, `ABSENT`, and `EXCUSED` attendance rules
- Idempotent duplicate QR-scan handling
- Optional location-policy validation
- Attendance events and audit-style records
- Absence-request workflow and transition hardening
- Notification and email-outbox infrastructure
- Check-in attempt logs and fraud-incident support
- MySQL schema managed by Flyway
- OpenAPI contract

### Engineering and Local Infrastructure

- Docker Compose for MySQL, Redis, Mailpit, and the backend
- Maven Wrapper
- GitHub Actions backend CI with a MySQL 8 service
- Surefire test-report artifacts
- Product screenshots and architecture documentation

## Status

This is a **production-like academic and portfolio project**, not a deployed production system. Approximately 90% of the original planned scope is implemented.

## Known Limitations

- Some client configuration is still optimized for local development
- Some lecturer dashboard values may use fallback presentation data when API data is unavailable
- No formal performance or scalability claim is made without load-test evidence
- Notification delivery has not been validated as a high-scale production service
- Fraud support is rule/evidence-based monitoring, not autonomous fraud detection
- A secondary Android workspace remains in the repository and should be clarified or archived

## Recommended Release Assets

GitHub automatically provides source archives. An Android APK may be attached later after producing a reviewed release build and verifying that it contains no secrets or development-only endpoints.
