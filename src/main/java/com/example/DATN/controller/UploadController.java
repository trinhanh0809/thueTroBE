package com.example.DATN.controller;

import com.example.DATN.service.UploadImage.UploadImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class UploadController {

    private final UploadImageService uploadImageService;

    /** Upload ảnh: trả về secure_url (JWT tuỳ bạn muốn bảo vệ hay public) */
    @PostMapping("/images")
    @PreAuthorize("isAuthenticated()") // hoặc hasRole('HOST') tuỳ nghiệp vụ
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name
    ) {
        String url = uploadImageService.uploadImage(file, name);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /** Xoá theo URL Cloudinary */
    @DeleteMapping("/images")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteByUrl(@RequestParam("url") String url) {
        uploadImageService.deleteImage(url);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
