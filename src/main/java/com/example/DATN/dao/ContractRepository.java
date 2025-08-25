package com.example.DATN.dao;

import com.example.DATN.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByRoom_Host_IdUser(Integer hostId);
    List<Contract> findByTenant_IdUser(Integer tenantId);
    Optional<Contract> findFirstByRoom_IdAndActiveTrueOrderByStartDateDesc(Long roomId);
}
