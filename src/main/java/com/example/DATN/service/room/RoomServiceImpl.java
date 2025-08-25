// com/example/DATN/service/room/RoomServiceImpl.java
package com.example.DATN.service.room;

import com.example.DATN.dao.*;
import com.example.DATN.dto.room.*;
import com.example.DATN.entity.*;
import com.example.DATN.enums.AreaType;
import com.example.DATN.enums.RoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final UserRepository userRepo;
    private final AreaRepository areaRepo;
    private final RoomTypeRepository roomTypeRepo;
    private final AmenityRepository amenityRepo;
    private final RoomRepository roomRepo;
    private final RoomImageRepository roomImageRepo;
    private final RoomAmenityRepository roomAmenityRepo;

    @Override
    public RoomDetailDto createRoomPending(String currentUsername, CreateRoomRequest req) {
        var host = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user hiện tại"));
        if (!host.isHost()) throw new IllegalStateException("Tài khoản chưa được duyệt làm chủ trọ");

        Area ward = areaRepo.findById(req.wardId())
                .orElseThrow(() -> new IllegalArgumentException("Ward không tồn tại"));
        if (ward.getType() != AreaType.WARD) throw new IllegalArgumentException("wardId phải là AREA type=WARD");

        RoomType roomType = null;
        if (req.roomTypeId() != null) {
            roomType = roomTypeRepo.findById(req.roomTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("RoomType không tồn tại"));
        }

        List<Amenity> amenities = List.of();
        if (req.amenityIds() != null && !req.amenityIds().isEmpty()) {
            amenities = amenityRepo.findAllById(req.amenityIds());
            if (amenities.size() != req.amenityIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều amenityId không hợp lệ");
            }
        }

        Room room = new Room();
        room.setHost(host);
        room.setRoomType(roomType);
        room.setArea(ward);
        room.setAddressLine(req.addressLine());
        room.setTitle(req.title());
        room.setDescription(req.description());
        room.setPriceMonth(req.priceMonth());
        room.setDeposit(req.deposit());
        room.setElectricityPrice(req.electricityPrice());
        room.setWaterPrice(req.waterPrice());
        room.setAreaSqm(req.areaSqm());
        room.setMaxOccupancy(req.maxOccupancy());
        room.setLat(req.lat());
        room.setLng(req.lng());

        // QUAN TRỌNG: gửi duyệt
        room.setStatus(RoomStatus.PENDING);
        room.setCreatedAt(Instant.now());
        room.setUpdatedAt(Instant.now());

        Room saved = roomRepo.save(room);

        // Ảnh
        if (req.imageUrls() != null && !req.imageUrls().isEmpty()) {
            List<RoomImage> imgs = new ArrayList<>();
            for (int i = 0; i < req.imageUrls().size(); i++) {
                var img = new RoomImage();
                img.setRoom(saved);
                img.setUrl(req.imageUrls().get(i));
                img.setCover(i == 0);
                img.setSortOrder(i + 1);
                img.setUploadedAt(Instant.now());
                imgs.add(img);
            }
            roomImageRepo.saveAll(imgs);
        }

        // Tiện ích
        if (!amenities.isEmpty()) {
            List<RoomAmenity> ras = new ArrayList<>();
            for (Amenity a : amenities) {
                var id = new RoomAmenityId(saved.getId(), a.getId());
                var ra = new RoomAmenity();
                ra.setId(id);
                ra.setRoom(saved);
                ra.setAmenity(a);
                ras.add(ra);
            }
            roomAmenityRepo.saveAll(ras);
        }

        return toDetailDto(saved);
    }

    @Override
    public RoomDetailDto approve(String adminUsername, Long roomId) {
        var admin = userRepo.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy admin hiện tại"));

        var room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        // chỉ cho approve từ PENDING
        if (room.getStatus() != RoomStatus.PENDING) {
            throw new IllegalStateException("Chỉ được duyệt phòng ở trạng thái PENDING");
        }

        room.setStatus(RoomStatus.APPROVED);
        room.setApprovedBy(admin);
        room.setApprovedAt(Instant.now());
        room.setUpdatedAt(Instant.now());
        roomRepo.save(room);

        return toDetailDto(room);
    }

    @Override
    public RoomDetailDto reject(String adminUsername, Long roomId, String note) {
        userRepo.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy admin hiện tại"));

        var room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        if (room.getStatus() != RoomStatus.PENDING) {
            throw new IllegalStateException("Chỉ được từ chối phòng ở trạng thái PENDING");
        }

        room.setStatus(RoomStatus.REJECTED);
        room.setApprovedBy(null);
        room.setApprovedAt(null);
        room.setUpdatedAt(Instant.now());
        // TODO: nếu cần lưu lý do từ chối, thêm cột moderation_note vào Room
        roomRepo.save(room);

        return toDetailDto(room);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomListDto> listApproved(Pageable pageable) {
        return roomRepo.findByStatus(RoomStatus.APPROVED, pageable)
                .map(this::toListDto);
    }

    // ====== mapping helpers ======

    private RoomDetailDto toDetailDto(Room r) {
        String wardName = r.getArea() != null ? r.getArea().getName() : null;
        String districtName = (r.getArea()!=null && r.getArea().getParent()!=null) ? r.getArea().getParent().getName() : null;
        String provinceName = (r.getArea()!=null && r.getArea().getParent()!=null && r.getArea().getParent().getParent()!=null)
                ? r.getArea().getParent().getParent().getName() : null;

        List<String> imageUrls = roomImageRepo.findByRoom_IdOrderBySortOrderAsc(r.getId())
                .stream().map(RoomImage::getUrl).toList();

        List<String> amenityNames = roomAmenityRepo.findByRoom_Id(r.getId())
                .stream().map(ra -> ra.getAmenity().getName()).toList();

        RoomType rt = r.getRoomType();
        RoomTypeDto typeDto = (rt == null) ? null : new RoomTypeDto(rt.getId(), rt.getCode(), rt.getName(), rt.getSortOrder());

        return new RoomDetailDto(
                r.getId(),
                r.getTitle(),
                r.getDescription(),
                r.getAddressLine(),
                r.getPriceMonth(),
                r.getDeposit(),
                r.getElectricityPrice(),
                r.getWaterPrice(),
                r.getAreaSqm(),
                r.getMaxOccupancy(),
                r.getStatus().name(),
                r.getLat(),
                r.getLng(),
                typeDto,
                amenityNames,
                imageUrls,
                wardName, districtName, provinceName
        );
    }

    private RoomListDto toListDto(Room r) {
        String cover = roomImageRepo.findByRoom_IdOrderBySortOrderAsc(r.getId())
                .stream().findFirst().map(RoomImage::getUrl).orElse(null);

        String wardName = r.getArea() != null ? r.getArea().getName() : null;
        String districtName = (r.getArea()!=null && r.getArea().getParent()!=null) ? r.getArea().getParent().getName() : null;
        String provinceName = (r.getArea()!=null && r.getArea().getParent()!=null && r.getArea().getParent().getParent()!=null)
                ? r.getArea().getParent().getParent().getName() : null;

        return new RoomListDto(
                r.getId(),
                r.getTitle(),
                cover,
                r.getPriceMonth(),
                r.getAreaSqm(),
                wardName, districtName, provinceName
        );
    }
}
