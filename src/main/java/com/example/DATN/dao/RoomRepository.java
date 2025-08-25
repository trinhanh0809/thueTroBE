package com.example.DATN.dao;


import com.example.DATN.entity.Room;
import com.example.DATN.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    List<Room> findByHost_IdUser(Integer hostId);
    long countByHost_IdUser(Integer hostId);
    Page<Room> findByStatus(RoomStatus status, Pageable pageable);
}
