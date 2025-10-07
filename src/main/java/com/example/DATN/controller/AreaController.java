package com.example.DATN.controller;

import com.example.DATN.dao.AreaRepository;
import com.example.DATN.dto.area.AreaOptionDto;
import com.example.DATN.entity.Area;
import com.example.DATN.enums.AreaType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaRepository areaRepo;

    @GetMapping("/provinces")
    public ResponseEntity<List<AreaOptionDto>> provinces() {
        var list = areaRepo.findByTypeOrderByNameAsc(AreaType.PROVINCE)
                .stream().map(this::toOption).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/districts")
    public ResponseEntity<List<AreaOptionDto>> districts(@RequestParam Long provinceId) {
        var list = areaRepo.findByTypeAndParent_IdOrderByNameAsc(AreaType.DISTRICT, provinceId)
                .stream().map((Area a) -> toOption(a)).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }


    @GetMapping("/wards")
    public ResponseEntity<List<AreaOptionDto>> wards(@RequestParam Long districtId) {
        var list = areaRepo.findByTypeAndParent_IdOrderByNameAsc(AreaType.WARD, districtId)
                .stream().map(this::toOption).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }


    @GetMapping("/wards-by-province")
    public ResponseEntity<List<AreaOptionDto>> wardsByProvince(@RequestParam Long provinceId) {
        var list = areaRepo.findByTypeAndParent_Parent_IdOrderByNameAsc(AreaType.WARD, provinceId)
                .stream().map(this::toOption).collect(Collectors.toList());
        // hoặc areaRepo.findWardsByProvinceId(provinceId) nếu bạn dùng @Query ở trên
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
