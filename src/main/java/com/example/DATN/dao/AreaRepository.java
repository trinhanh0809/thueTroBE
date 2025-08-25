package com.example.DATN.dao;

import com.example.DATN.entity.Area;
import com.example.DATN.enums.AreaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {
    List<Area> findByTypeOrderByNameAsc(AreaType type);
    List<Area> findByParent_IdOrderByNameAsc(Long parentId);
}