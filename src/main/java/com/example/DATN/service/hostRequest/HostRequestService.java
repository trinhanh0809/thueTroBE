package com.example.DATN.service.hostRequest;

import com.example.DATN.dto.host.HostRequestDto;

import java.util.List;

public interface HostRequestService {
    void approve(Long requestId, String adminUsername, String note);
    void reject(Long requestId, String adminUsername, String note);

}
