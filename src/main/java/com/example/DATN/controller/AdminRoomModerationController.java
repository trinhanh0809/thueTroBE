// com/example/DATN/controller/AdminRoomModerationController.java
package com.example.DATN.controller;

import com.example.DATN.dto.room.ModerationDecisionRequest;
import com.example.DATN.dto.room.RoomDetailDto;
import com.example.DATN.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomModerationController {

    private final RoomService roomService;

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDetailDto> approve(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User admin,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(roomService.approve(admin.getUsername(), id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDetailDto> reject(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User admin,
            @PathVariable Long id,
            @RequestBody(required = false) ModerationDecisionRequest body
    ) {
        String note = body == null ? null : body.note();
        return ResponseEntity.ok(roomService.reject(admin.getUsername(), id, note));
    }
}
