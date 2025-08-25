package com.example.DATN.controller;


import com.example.DATN.dao.RoomTypeRepository;
import com.example.DATN.dto.roomType.RoomTypeDto;
import com.example.DATN.dto.roomType.RoomTypeUpsertReq;
import com.example.DATN.entity.RoomType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/room-types") // đã được SecurityConfig chặn: chỉ ADMIN truy cập
public class AdminRoomTypeController {

    private final RoomTypeRepository repo;

    // CREATE: POST /admin/room-types
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RoomTypeUpsertReq req) {
        if (repo.existsByCode(req.code())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Code already exists");
        }
        RoomType r = new RoomType();
        r.setCode(req.code());
        r.setName(req.name());
        r.setSortOrder(req.sortOrder());
        // r.setActive(true); // nếu muốn giữ mặc định, không dùng thì thôi

        r = repo.save(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(RoomTypeDto.from(r));
    }
    @GetMapping

    public ResponseEntity<?> list() {
        var list = repo.findAll().stream().map(RoomTypeDto::from).toList();
        return ResponseEntity.ok(list);
    }

    // UPDATE: PUT /admin/room-types/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody RoomTypeUpsertReq req) {
        return repo.findById(id)
                .map(r -> {
                    if (repo.existsByCodeAndIdNot(req.code(), id)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Code already exists");
                    }
                    r.setCode(req.code());
                    r.setName(req.name());
                    r.setSortOrder(req.sortOrder());
                    r = repo.save(r);
                    return ResponseEntity.ok(RoomTypeDto.from(r));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room type not found"));
    }

    // DELETE: DELETE /admin/room-types/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room type not found");
        }
        try {
            repo.deleteById(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (DataIntegrityViolationException ex) {
            // Nếu đang bị tham chiếu bởi bảng Room, DB sẽ chặn xoá → trả 409
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete: this room type is referenced by other records");
        }
    }
}
