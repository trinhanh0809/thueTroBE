package com.example.DATN.dao;

import com.example.DATN.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}