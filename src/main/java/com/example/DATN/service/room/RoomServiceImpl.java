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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl {

    private final UserRepository userRepo;
    private final BlockRepository blockRepo;
    private final AreaRepository areaRepo;
    private final AmenityRepository amenityRepo;
    private final RoomRepository roomRepo;
    private final RoomImageRepository roomImageRepo;
    private final RoomAmenityRepository roomAmenityRepo;
    private final RoomTypeRepository roomTypeRepo;

    // ===== CREATE in BLOCK =====
    public RoomDetailDto createRoomInBlock(String currentUsername, Long blockId, CreateRoomRequest req) {
        var host = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user hiện tại"));
        if (!host.isHost()) throw new IllegalStateException("Tài khoản chưa được duyệt làm chủ trọ");

        var block = blockRepo.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("Block không tồn tại"));

        // Validate WARD
        var ward = areaRepo.findById(req.wardId())
                .orElseThrow(() -> new IllegalArgumentException("Ward không tồn tại"));
        if (ward.getType() != AreaType.WARD) {
            throw new IllegalArgumentException("wardId phải là AREA type=WARD");
        }

        // Amenities
        List<Amenity> amenities = List.of();
        if (req.amenityIds() != null && !req.amenityIds().isEmpty()) {
            amenities = amenityRepo.findAllById(req.amenityIds());
            if (amenities.size() != req.amenityIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều amenityId không hợp lệ");
            }
        }

        var room = new Room();
        room.setHost(host);
        room.setBlock(block);
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

        // Public ngay
        room.setStatus(RoomStatus.APPROVED);
        room.setApprovedBy(host);
        room.setApprovedAt(Instant.now());
        room.setCreatedAt(Instant.now());
        room.setUpdatedAt(Instant.now());

        var saved = roomRepo.save(room);

        // Images
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

        // Amenities link
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

    public RoomDetailDto createRoom(String currentUsername, CreateRoomRequest req) {
        var host = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user hiện tại"));
        if (!host.isHost()) throw new IllegalStateException("Tài khoản chưa được duyệt làm chủ trọ");

        if (req.blockId() == null) throw new IllegalArgumentException("blockId là bắt buộc");
        var block = blockRepo.findById(req.blockId())
                .orElseThrow(() -> new IllegalArgumentException("Block không tồn tại"));
        // (Nếu block thuộc về user đăng nhập, dùng:)
        // var block = blockRepo.findByIdAndOwner_Username(req.blockId(), currentUsername)
        //        .orElseThrow(() -> new IllegalArgumentException("Block không thuộc quyền sở hữu"));

        var ward = areaRepo.findById(req.wardId())
                .orElseThrow(() -> new IllegalArgumentException("Ward không tồn tại"));
        // validate ward.getType() == WARD nếu cần

        var roomType = roomTypeRepo.findById(req.roomTypeId())
                .orElseThrow(() -> new IllegalArgumentException("RoomType không tồn tại"));
        List<Amenity> amenities = List.of();
        if (req.amenityIds() != null && !req.amenityIds().isEmpty()) {
            amenities = amenityRepo.findAllById(req.amenityIds());
            if (amenities.size() != req.amenityIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều amenityId không hợp lệ");
            }
        }

        var room = new Room();
        room.setHost(host);
        room.setBlock(block);
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
        room.setStatus(RoomStatus.APPROVED);
        room.setApprovedBy(host);
        room.setApprovedAt(Instant.now());

        var saved = roomRepo.save(room);

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

        // Amenities link
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


    // ===== LIST & DETAIL =====
    @Transactional(readOnly = true)
    public Page<RoomListDto> listAll(Pageable pageable) {
        return roomRepo.findByStatus(RoomStatus.APPROVED, pageable)
                .map(this::toListDto);
    }

    @Transactional(readOnly = true)
    public Page<RoomListDto> listByBlock(Long blockId, Pageable pageable) {
        return roomRepo.findByBlock_IdAndStatus(blockId, RoomStatus.APPROVED, pageable)
                .map(this::toListDto);
    }
    @Transactional(readOnly = true)
    public Page<RoomListDto> listByCurrentHost(String currentUsername, Pageable pageable) {
        return roomRepo.findByHost_Username(currentUsername, pageable)
                .map(this::toListDto);
    }

    @Transactional(readOnly = true)
    public RoomDetailDto getDetail(Long roomId) {
        var room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));
        return toDetailDto(room);
    }

    // ===== UPDATE =====
    public RoomDetailDto updateRoom(String currentUsername, Long roomId, UpdateRoomRequest req) {
        var host = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user hiện tại"));
        var room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        boolean isOwner = room.getHost() != null
                && Objects.equals(room.getHost().getIdUser(), host.getIdUser());
        if (!isOwner) throw new IllegalStateException("Bạn không có quyền sửa phòng này");

        // Đổi dãy (nếu cho phép)
        if (req.blockId() != null) {
            var block = blockRepo.findById(req.blockId())
                    .orElseThrow(() -> new IllegalArgumentException("Block không tồn tại"));
            room.setBlock(block);
        }

        // Đổi khu vực
        if (req.wardId() != null) {
            var ward = areaRepo.findById(req.wardId())
                    .orElseThrow(() -> new IllegalArgumentException("Ward không tồn tại"));
            if (ward.getType() != AreaType.WARD) {
                throw new IllegalArgumentException("wardId phải là AREA type=WARD");
            }
            room.setArea(ward);
        }
        if (req.roomTypeId() != null) {
            var roomType = roomTypeRepo.findById(req.roomTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("RoomType không tồn tại"));
            room.setRoomType(roomType);
        }

        // Replace fields
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
        room.setUpdatedAt(Instant.now());

        // Replace amenities
        if (req.amenityIds() != null) {
            roomAmenityRepo.deleteByRoom_Id(room.getId());
            if (!req.amenityIds().isEmpty()) {
                var amenities = amenityRepo.findAllById(req.amenityIds());
                if (amenities.size() != req.amenityIds().size()) {
                    throw new IllegalArgumentException("Một hoặc nhiều amenityId không hợp lệ");
                }
                List<RoomAmenity> ras = new ArrayList<>();
                for (Amenity a : amenities) {
                    var id = new RoomAmenityId(room.getId(), a.getId());
                    var ra = new RoomAmenity();
                    ra.setId(id);
                    ra.setRoom(room);
                    ra.setAmenity(a);
                    ras.add(ra);
                }
                roomAmenityRepo.saveAll(ras);
            }
        }

        // Replace images
        if (req.imageUrls() != null) {
            roomImageRepo.deleteByRoom_Id(room.getId());
            if (!req.imageUrls().isEmpty()) {
                List<RoomImage> imgs = new ArrayList<>();
                for (int i = 0; i < req.imageUrls().size(); i++) {
                    var img = new RoomImage();
                    img.setRoom(room);
                    img.setUrl(req.imageUrls().get(i));
                    img.setCover(i == 0);
                    img.setSortOrder(i + 1);
                    img.setUploadedAt(Instant.now());
                    imgs.add(img);
                }
                roomImageRepo.saveAll(imgs);
            }
        }

        var saved = roomRepo.save(room);
        return toDetailDto(saved);
    }

    // ===== DELETE =====
    public void deleteRoom(String currentUsername, Long roomId) {
        var host = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user hiện tại"));
        var room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        boolean isOwner = room.getHost() != null
                && Objects.equals(room.getHost().getIdUser(), host.getIdUser());
        if (!isOwner) throw new IllegalStateException("Bạn không có quyền xoá phòng này");

        roomImageRepo.deleteByRoom_Id(roomId);
        roomAmenityRepo.deleteByRoom_Id(roomId);
        roomRepo.delete(room);
    }

    // ===== SEARCH (có filter block) =====
    @Transactional(readOnly = true)
    public Page<RoomListDto> searchWithBlock(
            Long provinceId,
            Long districtId,
            Long wardId,
            Long blockId,
            BigDecimal priceMin,
            BigDecimal priceMax,
            Integer areaMin,
            Integer areaMax,
            String keyword,
            Long roomTypeId,
            Pageable pageable
    ) {
        var q = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        var page = roomRepo.searchRooms(
                RoomStatus.APPROVED,
                provinceId, districtId, wardId,
                blockId,
                priceMin, priceMax,
                areaMin, areaMax,
                roomTypeId,
                q,
                pageable
        );
        return page.map(this::toListDto);
    }



    // ====== mapping helpers ======
    private RoomDetailDto toDetailDto(Room r) {
        // Hành chính: phường/xã -> quận/huyện -> tỉnh/thành
        Area ward = r.getArea();
        Area district = (ward != null) ? ward.getParent() : null;
        Area province = (district != null) ? district.getParent() : null;

        Long wardId = (ward != null) ? ward.getId() : null;
        Long districtId = (district != null) ? district.getId() : null;
        Long provinceId = (province != null) ? province.getId() : null;

        // Ảnh phòng
        var imageUrls = roomImageRepo.findByRoom_IdOrderBySortOrderAsc(r.getId())
                .stream()
                .map(RoomImage::getUrl)
                .toList();

        // Tiện ích
        var amenityIds = roomAmenityRepo.findByRoom_Id(r.getId())
                .stream()
                .map(ra -> ra.getAmenity().getId())
                .toList();

        // RoomType
        var rt = r.getRoomType();
        Long roomTypeId   = (rt != null) ? rt.getId()   : null;   // đổi getter nếu khác
        String roomTypeName = (rt != null) ? rt.getName() : null; // đổi getter nếu khác

        // Block
        var blk = r.getBlock();
        Long blockId = (blk != null) ? blk.getId() : null;        // đổi getter nếu khác

        // Contact từ host
        var u = r.getHost();
        ContactDto contact = null;
        if (u != null) {
            String displayName = ((u.getFirstName() == null ? "" : u.getFirstName()) + " " +
                    (u.getLastName()  == null ? "" : u.getLastName())).trim();
            if (displayName.isEmpty()) displayName = u.getUsername();
            contact = new ContactDto(u.getIdUser(), displayName, u.getPhoneNumber(), u.getAvatar());
        }

        // Trả về DTO (đã thêm roomTypeId, roomTypeName, blockId)
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
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getLat(),
                r.getLng(),
                roomTypeId,        // Long
                roomTypeName,      // String
                blockId,           // Long
                amenityIds,
                imageUrls,
                wardId, districtId, provinceId,
                contact
        );

    }


    private RoomListDto toListDto(Room r) {
        String wardName = (r.getArea() != null) ? r.getArea().getName() : null;
        String districtName = (r.getArea() != null && r.getArea().getParent() != null)
                ? r.getArea().getParent().getName() : null;
        String provinceName = (r.getArea() != null && r.getArea().getParent() != null
                && r.getArea().getParent().getParent() != null)
                ? r.getArea().getParent().getParent().getName() : null;

        var imageUrls = roomImageRepo.findByRoom_IdOrderBySortOrderAsc(r.getId())
                .stream().map(RoomImage::getUrl).toList();

        var amenityNames = roomAmenityRepo.findByRoom_Id(r.getId())
                .stream().map(ra -> ra.getAmenity().getName()).toList();

        // room type & block
        var rt = r.getRoomType();
        Long roomTypeId = (rt != null) ? rt.getId() : null;        // đổi getter nếu khác
        String roomTypeName = (rt != null) ? rt.getName() : null;

        var blk = r.getBlock();
        Long blockId = (blk != null) ? blk.getId() : null;         // đổi getter nếu khác

        return new RoomListDto(
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
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getLat(),
                r.getLng(),

                roomTypeId,
                roomTypeName,
                blockId,

                amenityNames,
                imageUrls,
                wardName, districtName, provinceName
        );
    }


    @Transactional(readOnly = true)
    public Page<RoomListDto> listAllByBlock(Long blockId, Pageable pageable) {
        return roomRepo.findByBlock_Id(blockId, pageable)
                .map(this::toListDto);
    }
}
