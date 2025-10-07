package com.example.DATN.service.block;

import com.example.DATN.dao.BlockRepository;
import com.example.DATN.dao.UserRepository;
import com.example.DATN.dto.block.BlockRequest;
import com.example.DATN.dto.block.BlockResponse;
import com.example.DATN.entity.Block;
import com.example.DATN.entity.User;
import com.example.DATN.service.block.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepo;
    private final UserRepository userRepo;

    /* ========= Mapper ========= */
    private BlockResponse toResponse(Block b) {
        return new BlockResponse(
                b.getId(),
                b.getName(),
                b.getAddress(),
                b.getCoverImageUrl(),
                b.isEnable(),
                b.getCreatedAt(),
                b.getCount_room()
        );
    }

    /* ========= CRUD ========= */
    @Override
    @Transactional
    public BlockResponse create(BlockRequest req) {
        Block b = Block.builder()
                .name(req.name())
                .address(req.address())
                .coverImageUrl(req.coverImageUrl())
                .enable(req.enable() == null ? true : req.enable())
                .build();

        // Gắn owner nếu có
        if (req.ownerId() != null) {
            User owner = userRepo.findById(req.ownerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + req.ownerId()));
            b.setOwner(owner);
        }

        blockRepo.save(b);
        return toResponse(b);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlockResponse> list(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return blockRepo.findAll(pageable).map(this::toResponse);
        }
        return blockRepo.findByNameContainingIgnoreCase(q.trim(), pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BlockResponse get(Long id) {
        Block b = blockRepo.findById(id).orElseThrow();
        return toResponse(b);
    }

    @Override
    @Transactional
    public BlockResponse update(Long id, BlockRequest req) {
        Block b = blockRepo.findById(id).orElseThrow();

        if (req.name() != null) b.setName(req.name());
        if (req.address() != null) b.setAddress(req.address());
        if (req.coverImageUrl() != null) b.setCoverImageUrl(req.coverImageUrl());
        if (req.enable() != null) b.setEnable(req.enable());

        if (req.ownerId() != null) {
            User owner = userRepo.findById(req.ownerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + req.ownerId()));
            b.setOwner(owner);
        }

        return toResponse(b);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        blockRepo.deleteById(id);
    }

    /* ========= Cover helpers ========= */
    @Override
    @Transactional
    public BlockResponse updateCover(Long id, String url) {
        Block b = blockRepo.findById(id).orElseThrow();
        b.setCoverImageUrl(url);
        return toResponse(b);
    }

    @Override
    @Transactional
    public void removeCover(Long id) {
        Block b = blockRepo.findById(id).orElseThrow();
        b.setCoverImageUrl(null);
    }

    /* ========= Enable helpers ========= */
    @Override
    @Transactional
    public BlockResponse toggleEnable(Long id, boolean enable) {
        Block b = blockRepo.findById(id).orElseThrow();
        b.setEnable(enable);
        return toResponse(b);
    }

    /* ========= For FE convenience ========= */
    @Override
    @Transactional(readOnly = true)
    public String getCoverUrl(Long id) {
        Block b = blockRepo.findById(id).orElseThrow();
        return b.getCoverImageUrl();
    }
}
