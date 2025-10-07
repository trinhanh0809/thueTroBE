// com/example/DATN/service/room/RoomService.java
package com.example.DATN.service.room;

import com.example.DATN.dto.room.CreateRoomRequest;
import com.example.DATN.dto.room.UpdateRoomRequest;
import com.example.DATN.dto.room.RoomDetailDto;
import com.example.DATN.dto.room.RoomListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface RoomService {
    // CREATE (không cần duyệt admin)
    RoomDetailDto createRoom(String currentUsername, CreateRoomRequest req);

    // READ
    Page<RoomListDto> listAll(Pageable pageable);
    Page<RoomListDto> listByHost(String hostUsername, Pageable pageable);
    Page<RoomListDto> listByCurrentHost(String currentUsername, Pageable pageable);
    RoomDetailDto getDetail(Long roomId);

    // UPDATE + DELETE (bạn đang gọi ở controller)
    RoomDetailDto updateRoom(String currentUsername, Long roomId, UpdateRoomRequest req);
    void deleteRoom(String currentUsername, Long roomId);

    Page<RoomListDto> search(
            Long provinceId,
            Long districtId,
            Long wardId,
            Long roomTypeId,
            BigDecimal priceMin,
            BigDecimal priceMax,
            Integer areaMin,
            Integer areaMax,
            Pageable pageable
    );
}
