// com/example/DATN/controller/RoomController.java
package com.example.DATN.controller;

import com.example.DATN.dto.room.*;
import com.example.DATN.service.room.RoomServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomServiceImpl roomService;

    // ===== LIST PUBLIC (APPROVED) =====
    @GetMapping
    public ResponseEntity<PageDto<RoomListDto>> list(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(PageDto.of(roomService.listAll(pageable)));
    }

    // ===== LIST CỦA CHÍNH HOST ĐANG ĐĂNG NHẬP =====
    // Trả về các phòng do user hiện tại đăng (host), phục vụ trang "bài viết của tôi"
    @GetMapping("/me")
    public ResponseEntity<PageDto<RoomListDto>> listByCurrentHost(
            @AuthenticationPrincipal(expression = "username") String currentUsername,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(PageDto.of(roomService.listByCurrentHost(currentUsername, pageable)));
    }

    // ===== SEARCH (có filter block) =====
    @GetMapping("/search")
    public ResponseEntity<PageDto<RoomListDto>> search(
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Integer areaMin,
            @RequestParam(required = false) Integer areaMax,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false, name = "q") String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        var page = roomService.searchWithBlock(
                        provinceId, districtId, wardId, blockId,
                        priceMin, priceMax, areaMin, areaMax, keyword,
                roomTypeId,
                        pageable
                );
        return ResponseEntity.ok(PageDto.of(page));
    }


    // ===== DETAIL =====
    @GetMapping("/{id}")
    public ResponseEntity<RoomDetailDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getDetail(id));
    }

    // ===== CREATE (body có blockId giống roomTypeId) =====
    @PostMapping
    public ResponseEntity<RoomDetailDto> create(
            @AuthenticationPrincipal(expression = "username") String currentUsername,
            @RequestBody CreateRoomRequest req
    ) {
        var dto = roomService.createRoom(currentUsername, req);
        return ResponseEntity.status(201).body(dto);
    }

    // ===== UPDATE (chỉ host sở hữu phòng mới sửa được — check trong service) =====
    @PutMapping("/{id}")
    public ResponseEntity<RoomDetailDto> update(
            @AuthenticationPrincipal(expression = "username") String currentUsername,
            @PathVariable Long id,
            @RequestBody UpdateRoomRequest req
    ) {
        return ResponseEntity.ok(roomService.updateRoom(currentUsername, id, req));
    }

    // ===== DELETE (chỉ host sở hữu phòng mới xoá được — check trong service) =====
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal(expression = "username") String currentUsername,
            @PathVariable Long id
    ) {
        roomService.deleteRoom(currentUsername, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/blocks/{blockId}")
    public ResponseEntity<PageDto<RoomListDto>> listAllByBlock(
            @PathVariable Long blockId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(PageDto.of(roomService.listAllByBlock(blockId, pageable)));
    }
}
