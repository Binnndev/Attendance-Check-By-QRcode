package com.attendance.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChangePasswordRequest {

    @NotBlank
    @Size(min = 8, max = 200)
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 200)
    private String newPassword;

    @JsonIgnore
    private final Set<String> unknownFields = new LinkedHashSet<>();

    @JsonAnySetter
    public void captureUnknownField(String name, Object value) {
        unknownFields.add(name);
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @JsonIgnore
    public boolean hasUnknownFields() {
        return !unknownFields.isEmpty();
    }

    @JsonIgnore
    public Set<String> getUnknownFields() {
        return Collections.unmodifiableSet(unknownFields);
    }
}