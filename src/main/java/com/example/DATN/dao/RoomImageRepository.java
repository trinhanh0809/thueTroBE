package com.example.DATN.dao;

import com.example.DATN.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    List<RoomImage> findByRoom_IdOrderBySortOrderAsc(Long roomId);
    void deleteByRoom_Id(Long roomId);
}
