// com/example/DATN/controller/HostRoomController.java
package com.example.DATN.controller;

import com.example.DATN.dto.room.CreateRoomRequest;
import com.example.DATN.dto.room.RoomDetailDto;
import com.example.DATN.service.room.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/host/rooms")
@RequiredArgsConstructor
public class HostRoomController {

    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<RoomDetailDto> createRoom(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User me,
            @Valid @RequestBody CreateRoomRequest req
    ) {
        var dto = roomService.createRoomPending(me.getUsername(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
