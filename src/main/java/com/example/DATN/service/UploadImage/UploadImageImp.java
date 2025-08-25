package com.example.DATN.service.UploadImage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadImageImp implements UploadImageService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile multipartFile, String name) {
        try {
            // public_id: datn/<slug(name)>-<epochMillis>
            String safe = slug(name == null || name.isBlank() ? "img" : name);
            String publicId = "datn/" + safe + "-" + Instant.now().toEpochMilli();

            Map uploadResult = cloudinary.uploader().upload(
                    multipartFile.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "datn",           // thư mục gốc (trùng với prefix ở public_id)
                            "public_id", publicId,      // chỉ định public_id (kèm folder)
                            "overwrite", true,
                            "unique_filename", false,   // vì ta đã tự unique bằng timestamp
                            "invalidate", true
                            // có thể thêm transformation nếu muốn:
                            // "transformation", new Transformation().quality("auto").fetchFormat("auto")
                    )
            );

            return (String) uploadResult.get("secure_url"); // trả về HTTPS url
        } catch (Exception e) {
            throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String imgUrl) {
        try {
            String publicId = extractPublicIdFromUrl(imgUrl);
            if (publicId == null || publicId.isBlank()) {
                throw new IllegalArgumentException("Không trích được public_id từ URL Cloudinary");
            }
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            Object status = result.get("result");
            if (status == null || !"ok".equalsIgnoreCase(status.toString())) {
                // "not found" cũng coi là xong (idempotent)
                if (!"not found".equalsIgnoreCase(String.valueOf(status))) {
                    throw new RuntimeException("Xoá ảnh thất bại: " + result);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Xoá ảnh thất bại: " + e.getMessage(), e);
        }
    }

    // -------- Helpers --------

    /** slug đơn giản (a-z0-9-) */
    private String slug(String input) {
        String s = new String(input.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                .trim().toLowerCase();
        s = s.replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("(^-|-$)", "");
        return s.isBlank() ? "img" : s;
    }

    /**
     * Trích public_id từ URL Cloudinary.
     * URL dạng: https://res.cloudinary.com/<cloud>/image/upload/<transform...>/v123456789/folder/name.jpg
     * -> public_id = "folder/name"
     */
    private String extractPublicIdFromUrl(String url) {
        if (url == null) return null;
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx < 0) return null;
        String tail = url.substring(uploadIdx + "/upload/".length());

        // Bỏ phần transformations (nếu có) trước 'v<digits>' và bỏ luôn 'v<digits>'
        String[] parts = tail.split("/");
        int vIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].matches("^v\\d+$")) { vIndex = i; break; }
        }
        StringBuilder sb = new StringBuilder();
        if (vIndex >= 0) {
            for (int i = vIndex + 1; i < parts.length; i++) {
                if (sb.length() > 0) sb.append('/');
                sb.append(parts[i]);
            }
        } else {
            // Không thấy 'v123...', lấy nguyên phần tail
            sb.append(tail);
        }
        String pathWithExt = sb.toString();
        // cắt đuôi .jpg/.png...
        int dot = pathWithExt.lastIndexOf('.');
        String withoutExt = (dot > 0) ? pathWithExt.substring(0, dot) : pathWithExt;
        // Đảm bảo không còn query string
        int q = withoutExt.indexOf('?');
        return (q > 0) ? withoutExt.substring(0, q) : withoutExt;
    }
}
