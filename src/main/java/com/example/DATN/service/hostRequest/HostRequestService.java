package com.example.DATN.service.hostRequest;

public interface HostRequestService {
    void approve(Long requestId, String adminUsername, String note);
    void reject(Long requestId, String adminUsername, String note);
}
