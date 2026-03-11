package com.attendance.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class UpdateMeRequest {

    @Size(min = 2, max = 120)
    private String fullName;

    @Size(max = 500)
    private String avatarUrl;

    @JsonIgnore
    private final Set<String> unknownFields = new LinkedHashSet<>();

    @JsonAnySetter
    public void captureUnknownField(String name, Object value) {
        unknownFields.add(name);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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