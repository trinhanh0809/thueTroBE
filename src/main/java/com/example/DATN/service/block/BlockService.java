package com.example.DATN.service.block;

import com.example.DATN.dto.block.BlockRequest;
import com.example.DATN.dto.block.BlockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlockService {
    BlockResponse create(BlockRequest req);
    Page<BlockResponse> list(String q, Pageable pageable);
    BlockResponse get(Long id);
    BlockResponse update(Long id, BlockRequest req);
    void delete(Long id);

    BlockResponse updateCover(Long id, String url);
    void removeCover(Long id);
    BlockResponse toggleEnable(Long id, boolean enable);
    String getCoverUrl(Long id);
}
