// com/example/DATN/controller/BlockController.java
package com.example.DATN.controller;

import com.example.DATN.dto.block.BlockRequest;
import com.example.DATN.dto.block.BlockResponse;
import com.example.DATN.dto.room.PageDto;
import com.example.DATN.service.block.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/blocks")
@RequiredArgsConstructor
public class BlockController {
    private final BlockService service;

    // List + search (q), phân trang
    @GetMapping
    public PageDto<BlockResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BlockResponse> result = service.list(q, pageable);
        return PageDto.of(result);
    }


    // Lấy chi tiết
    @GetMapping("/{id}")
    public BlockResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // Tạo mới
    @PostMapping
    public BlockResponse create(@RequestBody BlockRequest req) {
        return service.create(req);
    }

    // Cập nhật
    @PutMapping("/{id}")
    public BlockResponse update(@PathVariable Long id, @RequestBody BlockRequest req) {
        return service.update(id, req);
    }

    // Xoá
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ===== Cover helpers ===== */

    // Set cover bằng URL (FE đã upload xong và có url)
    @PutMapping("/{id}/cover")
    public BlockResponse updateCover(@PathVariable Long id, @RequestParam String url) {
        return service.updateCover(id, url);
    }

    // Bỏ cover
    @DeleteMapping("/{id}/cover")
    public ResponseEntity<Void> removeCover(@PathVariable Long id) {
        service.removeCover(id);
        return ResponseEntity.noContent().build();
    }

    // Lấy URL cover (cho FE tiện)
    @GetMapping("/{id}/cover")
    public Map<String, String> getCover(@PathVariable Long id) {
        return Map.of("url", service.getCoverUrl(id));
    }

    /* ===== Enable helpers ===== */

    // Bật/tắt block
    @PutMapping("/{id}/enable")
    public BlockResponse toggleEnable(@PathVariable Long id, @RequestParam boolean enable) {
        return service.toggleEnable(id, enable);
    }
}
