package com.example.DATN.controller;

import com.example.DATN.service.hostRequest.HostRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/host-requests")
@RequiredArgsConstructor
public class AdminHostApprovalController {

    private final HostRequestService hostRequestService;

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User admin,
            @PathVariable Long id,
            @RequestParam(required = false) String note  // <-- thêm note từ query param
    ) {
        hostRequestService.approve(id, admin.getUsername(), note);
        return ResponseEntity.ok("Approved");
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User admin,
            @PathVariable Long id,
            @RequestParam(required = false) String note
    ) {
        hostRequestService.reject(id, admin.getUsername(), note);
        return ResponseEntity.ok("Rejected");
    }
}
