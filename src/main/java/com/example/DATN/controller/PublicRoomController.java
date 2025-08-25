// com/example/DATN/controller/PublicRoomController.java
package com.example.DATN.controller;

import com.example.DATN.dto.room.RoomListDto;
import com.example.DATN.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class PublicRoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<Page<RoomListDto>> listApproved(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(roomService.listApproved(pageable));
    }
}
