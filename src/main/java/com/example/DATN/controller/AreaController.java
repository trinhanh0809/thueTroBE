
package com.example.DATN.controller;

import com.example.DATN.dao.AreaRepository;
import com.example.DATN.dto.area.AreaOptionDto;
import com.example.DATN.entity.Area;
import com.example.DATN.enums.AreaType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/areas")
@RequiredArgsConstructor
@CrossOrigin // nếu FE khác domain
public class AreaController {

    private final AreaRepository areaRepo;

    /** Lấy danh sách TỈNH (parentId = null) */
    @GetMapping("/provinces")
    public ResponseEntity<List<AreaOptionDto>> getProvinces() {
        var list = areaRepo.findByTypeOrderByNameAsc(AreaType.PROVINCE)
                .stream()
                .map(this::toOption)
                .toList();
        return ResponseEntity.ok(list);
    }

    /** Lấy danh sách PHƯỜNG theo provinceId (parentId = provinceId) */
    @GetMapping("/wards")
    public ResponseEntity<List<AreaOptionDto>> getWardsByProvince(@RequestParam Long provinceId) {
        var list = areaRepo.findByParent_IdOrderByNameAsc(provinceId)
                .stream()
                .map(this::toOption)
                .toList();
        return ResponseEntity.ok(list);
    }

    private AreaOptionDto toOption(Area a) {
        return new AreaOptionDto(
                a.getId(),
                a.getName(),
                a.getParent() == null ? null : a.getParent().getId()
        );
    }
}
