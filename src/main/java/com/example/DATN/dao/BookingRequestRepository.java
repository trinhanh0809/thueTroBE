package com.example.DATN.dao;

import com.example.DATN.entity.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    List<BookingRequest> findByTenant_IdUser(Integer tenantId);
    List<BookingRequest> findByRoom_Host_IdUser(Integer hostId);
}
