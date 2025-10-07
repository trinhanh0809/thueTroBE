package com.example.DATN.controller;

import com.example.DATN.dao.AmenityRepository;
import com.example.DATN.dto.amenity.AmenityDto;
import com.example.DATN.dto.amenity.AmenityUpsertReq;
import com.example.DATN.dto.roomType.RoomTypeDto;
import com.example.DATN.dto.roomType.RoomTypeUpsertReq;
import com.example.DATN.entity.Amenity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/amenity")
public class AdminAmenityController {
    private final AmenityRepository repo;

    @PostMapping()
    private ResponseEntity<?> create(@Valid @RequestBody AmenityUpsertReq req) {
        Amenity r = new Amenity();
        r.setName(req.name());
        r.setCode(req.code());
        r.setSortOrder(req.sortOrder());

        r = repo.save(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(AmenityDto.from(r));
    }

    @GetMapping

    public ResponseEntity<?> list() {
        var list = repo.findAll().stream().map(AmenityDto::from).toList();
        System.out.println(list);
        return ResponseEntity.ok(list);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody AmenityUpsertReq req) {
        return repo.findById(id)
                .map(r -> {
                    if (repo.existsByCodeAndIdNot(req.code(), id)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Ma code da co roi");
                    }
                    r.setCode(req.code());
                    r.setName(req.name());
                    r.setSortOrder(req.sortOrder());
                    r = repo.save(r);
                    return ResponseEntity.ok(AmenityDto.from(r));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Khong tim thay"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Khong ton tai");
        }
        try {
            repo.deleteById(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (DataIntegrityViolationException ex) {
            // Nếu đang bị tham chiếu bởi bảng Room, DB sẽ chặn xoá → trả 409
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Dang su dung, khong the xoa");
        }
    }
}
