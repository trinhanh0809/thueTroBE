package com.example.DATN.service.UploadImage;

import org.springframework.web.multipart.MultipartFile;

public interface UploadImageService {
    /** Upload ảnh, trả về secure_url */
    String uploadImage(MultipartFile multipartFile, String name);

    /** Xoá ảnh theo URL (sẽ tự trích public_id từ URL) */
    void deleteImage(String imgUrl);
}
