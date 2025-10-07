package com.example.DATN.dao;

import com.example.DATN.entity.Room;
import com.example.DATN.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Page<Room> findByStatus(RoomStatus status, Pageable pageable);
    Page<Room> findByHost_Username(String username, Pageable pageable);
    Page<Room> findByHost_IdUser(Long idUser, Pageable pageable);
    Page<Room> findByBlock_IdAndStatus(Long blockId, RoomStatus status, Pageable pageable);
    Page<Room> findByBlock_Id(Long blockId, Pageable pageable);
    @Query("""
        SELECT r FROM Room r
        LEFT JOIN r.area a
        LEFT JOIN a.parent d
        LEFT JOIN d.parent prov
        LEFT JOIN r.block b
        LEFT JOIN r.roomType rt
        WHERE (:status     IS NULL OR r.status = :status)
          AND (:blockId    IS NULL OR b.id = :blockId)
          AND (:wardId     IS NULL OR a.id = :wardId)
          AND (:districtId IS NULL OR d.id = :districtId)
          AND (:provinceId IS NULL OR prov.id = :provinceId)
          AND (:priceMin   IS NULL OR r.priceMonth >= :priceMin)
          AND (:priceMax   IS NULL OR r.priceMonth <= :priceMax)
          AND (:areaMin    IS NULL OR r.areaSqm    >= :areaMin)
          AND (:areaMax    IS NULL OR r.areaSqm    <= :areaMax)
          AND (:roomTypeId  IS NULL OR rt.id = :roomTypeId)
          AND (
                :q IS NULL OR
                LOWER(r.addressLine) LIKE CONCAT('%', LOWER(:q), '%') 
               
          )
        """)
    Page<Room> searchRooms(
            @Param("status") RoomStatus status,
            @Param("provinceId") Long provinceId,
            @Param("districtId") Long districtId,
            @Param("wardId") Long wardId,
            @Param("blockId") Long blockId,
            @Param("priceMin") BigDecimal priceMin,
            @Param("priceMax") BigDecimal priceMax,
            @Param("areaMin") Integer areaMin,
            @Param("areaMax") Integer areaMax,
            @Param("roomTypeId") Long roomTypeId,
            @Param("q") String q,
            Pageable pageable
    );
}
