package com.example.DATN.dao;

import com.example.DATN.entity.RoomAmenity;
import com.example.DATN.entity.RoomAmenityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, RoomAmenityId> {
    List<RoomAmenity> findByRoom_Id(Long roomId);
    void deleteByRoom_Id(Long roomId);
}
