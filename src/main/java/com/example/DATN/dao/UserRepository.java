package com.example.DATN.dao;

import com.example.DATN.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    @Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN u.listRoles r
    WHERE (:enabled IS NULL OR u.enabled = :enabled)
      AND (:isHost IS NULL OR u.host = :isHost)
      AND (:role IS NULL OR :role = '' OR r.nameRole = :role)
      AND (
           :q IS NULL OR :q = '' OR
           LOWER(u.username)    LIKE LOWER(CONCAT('%', :q, '%')) 
      )
""")
    Page<User> searchUsers(@Param("q") String q,
                           @Param("enabled") Boolean enabled,
                           @Param("isHost") Boolean isHost,
                           @Param("role") String role,
                           Pageable pageable);

}
