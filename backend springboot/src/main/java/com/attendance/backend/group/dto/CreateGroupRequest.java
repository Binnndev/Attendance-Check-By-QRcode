package com.attendance.backend.group.dto;

import com.attendance.backend.domain.enums.ApprovalMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateGroupRequest {

    @NotBlank(message = "name is required")
    @Size(min = 3, max = 150, message = "name length must be between 3 and 150")
    private String name;

    @NotBlank(message = "code is required")
    @Size(max = 20, message = "code length must be <= 20")
    private String code;

    @Size(min = 6, max = 16, message = "joinCode length must be between 6 and 16")
    private String joinCode;

    @Size(max = 1000, message = "description length must be <= 1000")
    private String description;

    @Size(max = 30, message = "semester length must be <= 30")
    private String semester;

    @Size(max = 80, message = "room length must be <= 80")
    private String room;

    @NotNull(message = "approvalMode is required")
    private ApprovalMode approvalMode;

    private boolean allowAutoJoinOnCheckin;

    public CreateGroupRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public ApprovalMode getApprovalMode() {
        return approvalMode;
    }

    public void setApprovalMode(ApprovalMode approvalMode) {
        this.approvalMode = approvalMode;
    }

    public boolean getAllowAutoJoinOnCheckin() {
        return allowAutoJoinOnCheckin;
    }

    public boolean isAllowAutoJoinOnCheckin() {
        return allowAutoJoinOnCheckin;
    }

    public void setAllowAutoJoinOnCheckin(boolean allowAutoJoinOnCheckin) {
        this.allowAutoJoinOnCheckin = allowAutoJoinOnCheckin;
    }
}