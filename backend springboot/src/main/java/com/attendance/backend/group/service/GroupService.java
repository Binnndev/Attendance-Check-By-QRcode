package com.attendance.backend.group.service;

import com.attendance.backend.group.dto.CreateGroupRequest;
import com.attendance.backend.group.dto.GroupResponse;

import java.util.UUID;

public interface GroupService {
    GroupResponse createGroup(UUID callerUserId, CreateGroupRequest req);
}