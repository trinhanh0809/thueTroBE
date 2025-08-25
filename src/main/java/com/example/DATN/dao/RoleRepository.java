package com.example.DATN.dao;
import com.example.DATN.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByNameRole(String nameRole);
}
