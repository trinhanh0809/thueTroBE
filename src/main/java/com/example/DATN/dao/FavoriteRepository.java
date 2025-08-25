package com.example.DATN.dao;

import com.example.DATN.entity.Favorite;
import com.example.DATN.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    boolean existsByUser_IdUserAndRoom_Id(Integer userId, Long roomId);
    List<Favorite> findByUser_IdUser(Integer userId);
    long deleteByUser_IdUserAndRoom_Id(Integer userId, Long roomId);
}
