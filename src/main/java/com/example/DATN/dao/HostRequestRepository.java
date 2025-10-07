package com.example.DATN.dao;

import com.example.DATN.dto.projection.HostPendingView;
import com.example.DATN.entity.HostRequest;
import com.example.DATN.entity.User;
import com.example.DATN.enums.HostRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HostRequestRepository extends JpaRepository<HostRequest, Long> {

    // Có request PENDING cho user này không?
    boolean existsByUserAndStatus(User user, HostRequestStatus status);

    // Lấy request gần nhất theo status
    Optional<HostRequest> findTopByUserAndStatusOrderByCreatedAtDesc(User user, HostRequestStatus status);

    // Lấy request gần nhất (bất kể status)
    Optional<HostRequest> findTopByUserOrderByCreatedAtDesc(User user);
//    List<HostRequest> findByStatus(String status);

    @Query("""
        select 
            hr.id as hostRequestId,
            u.id  as userId,
            u.username as username,
            u.email as email,
            hr.note as note,
            hr.createdAt as createdAt
        from HostRequest hr
        join hr.user u
        where hr.status = com.example.DATN.enums.HostRequestStatus.PENDING
        order by hr.createdAt desc
    """)
    List<HostPendingView> findPendingHostAccounts();

    // HostRequestRepository.java
    @Query("""
  SELECT hr FROM HostRequest hr
  JOIN hr.user u
  WHERE hr.id = (
    SELECT MAX(h2.id) FROM HostRequest h2 WHERE h2.user = u
  )
  AND (:status IS NULL OR hr.status = :status)
  AND (
       :q IS NULL OR :q = '' OR
       LOWER(u.username)    LIKE LOWER(CONCAT('%', :q, '%')) OR
       LOWER(u.email)       LIKE LOWER(CONCAT('%', :q, '%')) OR
       LOWER(u.firstName)   LIKE LOWER(CONCAT('%', :q, '%')) OR
       LOWER(u.lastName)    LIKE LOWER(CONCAT('%', :q, '%')) OR
       LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :q, '%'))
  )
""")
    Page<HostRequest> searchLatestPerUser(
            @org.springframework.data.repository.query.Param("status") com.example.DATN.enums.HostRequestStatus status,
            @org.springframework.data.repository.query.Param("q") String q,
            org.springframework.data.domain.Pageable pageable
    );

}
