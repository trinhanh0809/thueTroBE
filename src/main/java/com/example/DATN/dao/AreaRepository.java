package com.example.DATN.dao;

import com.example.DATN.entity.Area;
import com.example.DATN.enums.AreaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {
    List<Area> findByTypeOrderByNameAsc(AreaType type);

    // Districts by province
    List<Area> findByTypeAndParent_IdOrderByNameAsc(AreaType type, Long provinceId);

    // Wards by district
//    List<Area> findByTypeAndParent_IdOrderByNameAsc(AreaType type, Long districtId);

    // Wards by province (JOIN 2 cấp: ward.parent.parent.id = provinceId)
    List<Area> findByTypeAndParent_Parent_IdOrderByNameAsc(AreaType type, Long provinceId);
}