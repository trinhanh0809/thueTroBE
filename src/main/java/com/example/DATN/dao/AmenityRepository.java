package com.example.DATN.dao;


import com.example.DATN.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    List<Amenity> findByActiveTrueOrderBySortOrderAsc();
}
