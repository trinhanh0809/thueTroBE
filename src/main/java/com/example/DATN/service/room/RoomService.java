// com/example/DATN/service/room/RoomService.java
package com.example.DATN.service.room;

import com.example.DATN.dto.room.CreateRoomRequest;
import com.example.DATN.dto.room.RoomDetailDto;
import com.example.DATN.dto.room.RoomListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomService {
    RoomDetailDto createRoomPending(String currentUsername, CreateRoomRequest req);
    RoomDetailDto approve(String adminUsername, Long roomId);
    RoomDetailDto reject(String adminUsername, Long roomId, String note);
    Page<RoomListDto> listApproved(Pageable pageable);
}
