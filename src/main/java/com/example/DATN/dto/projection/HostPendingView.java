package com.example.DATN.dto.projection;

public interface HostPendingView {
    Long getHostRequestId();
    Long getUserId();
    String getUsername();
    String getEmail();
    String getNote();
    java.time.LocalDateTime getCreatedAt();
}

