// com/example/DATN/dao/BlockRepository.java
package com.example.DATN.dao;

import com.example.DATN.entity.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

public interface BlockRepository extends JpaRepository<Block, Long> {
    Page<Block> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Bật/tắt nhanh không cần load entity (tuỳ chọn)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Block b set b.enable = :enable where b.id = :id")
    int updateEnable(Long id, boolean enable);
}
