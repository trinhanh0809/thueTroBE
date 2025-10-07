package com.example.DATN.dto.block;

public record BlockRequest(
        String name,
        String address,
        Integer ownerId,          // optional: nếu có luồng set owner
        String coverImageUrl,  // optional: có thể null khi tạo
        Boolean enable         // optional: null => mặc định true
) {}
