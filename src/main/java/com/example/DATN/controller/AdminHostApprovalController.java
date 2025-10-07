package com.example.DATN.controller;

import com.example.DATN.dao.HostRequestRepository;                // DTO body { note }
import com.example.DATN.dto.host.HostApprovalRequest;
import com.example.DATN.entity.HostRequest;
import com.example.DATN.enums.HostRequestStatus;
import com.example.DATN.service.hostRequest.HostRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/host-requests")
@RequiredArgsConstructor
public class AdminHostApprovalController {

    private final HostRequestService hostRequestService;
    private final HostRequestRepository hostRequestRepo;          // dùng cho endpoint pending (projection)

    // Approve: chuyển PENDING -> APPROVED, set note, gán HOST + isHost = true
    @PutMapping("/{id}/approve")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User admin,
            @PathVariable Long id,
            @RequestBody(required = false) HostApprovalRequest body
    ) {
        String note = (body != null) ? body.getNote() : null;
        hostRequestService.approve(id, admin.getUsername(), note);
        return ResponseEntity.ok().body(
                java.util.Map.of("message", "Approved", "requestId", id, "approvedBy", admin.getUsername())
        );
    }

    // Reject: chuyển PENDING -> REJECTED, set note, bỏ HOST + isHost = false
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User admin,
            @PathVariable Long id,
            @RequestBody(required = false) HostApprovalRequest body // {"note":"..."} (optional)
    ) {
        String note = (body != null) ? body.getNote() : null;
        hostRequestService.reject(id, admin.getUsername(), note);
        return ResponseEntity.ok().body(
                java.util.Map.of("message", "Rejected", "requestId", id, "rejectedBy", admin.getUsername())
        );
    }

    // Danh sách tài khoản có yêu cầu host đang PENDING (projection)
//    @GetMapping("/pending")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> getAccountsWithHostPending() {
//        return ResponseEntity.ok(hostRequestRepo.findPendingHostAccounts());
//    }

    // Danh sách host-requests có search + phân trang
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listHostRequests(
            @RequestParam(required = false) HostRequestStatus status,
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction dir
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<HostRequest> p = hostRequestRepo.searchLatestPerUser(status, q, pageable);

        // map content
        List<Map<String, Object>> content = p.getContent().stream().map(hr -> {
            var u = hr.getUser();
            return Map.of(
                    "id", hr.getId(),
                    "status", hr.getStatus().name(),
                    "note", hr.getNote() == null ? "" : hr.getNote(),
                    "createdAt", hr.getCreatedAt(),
                    "user", Map.of(
                            "id", u.getIdUser(),
                            "username", u.getUsername(),
                            "email", u.getEmail(),
                            "isHost", u.isHost()
                    )
            );
        }).toList();

        // response chỉ chứa meta + content (tuỳ nhu cầu)
        Map<String, Object> response = Map.of(
                "totalElements", p.getTotalElements(),
                "totalPages", p.getTotalPages(),
                "pageNumber", p.getNumber(),
                "pageSize", p.getSize(),
                "content", content  // nếu không cần content thì bỏ luôn
        );

        return ResponseEntity.ok(response);
    }


}
