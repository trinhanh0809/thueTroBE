package com.example.DATN.dao;

import com.example.DATN.entity.HostRequest;
import com.example.DATN.entity.User;
import com.example.DATN.enums.HostRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostRequestRepository extends JpaRepository<HostRequest, Long> {

    // Có request PENDING cho user này không?
    boolean existsByUserAndStatus(User user, HostRequestStatus status);

    // Lấy request gần nhất theo status
    Optional<HostRequest> findTopByUserAndStatusOrderByCreatedAtDesc(User user, HostRequestStatus status);

    // Lấy request gần nhất (bất kể status)
    Optional<HostRequest> findTopByUserOrderByCreatedAtDesc(User user);
}
