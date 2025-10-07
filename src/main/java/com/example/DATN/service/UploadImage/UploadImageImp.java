// com/example/DATN/service/UploadImage/UploadImageImp.java
package com.example.DATN.service.UploadImage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadImageImp implements UploadImageService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String name) {
        try {
            String publicId = (name != null && !name.isBlank())
                    ? "rooms/" + name
                    : "rooms/" + UUID.randomUUID();

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", true,
                            "resource_type", "image"  // chỉ ảnh
                    )
            );
            // Trả secure_url
            return (String) uploadResult.getOrDefault("secure_url", uploadResult.get("url"));
        } catch (IOException e) {
            throw new RuntimeException("Upload thất bại", e);
        }
    }

    @Override
    public void deleteImage(String urlOrPublicId) {
        try {
            String publicId = extractPublicId(urlOrPublicId);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Xoá ảnh thất bại", e);
        }
    }

    private String extractPublicId(String urlOrId) {
        // Nếu client đưa luôn publicId thì dùng thẳng
        if (!urlOrId.contains("/")) return urlOrId;
        // Nếu là URL: tách phần path không có extension
        // ví dụ https://res.cloudinary.com/<cloud>/image/upload/v123/rooms/abc-uuid.jpg
        int idx = urlOrId.indexOf("/upload/");
        String path = (idx > 0) ? urlOrId.substring(idx + "/upload/".length()) : urlOrId;
        // bỏ version v123/
        path = path.replaceFirst("^v\\d+/", "");
        // bỏ extension
        int dot = path.lastIndexOf('.');
        return (dot > 0) ? path.substring(0, dot) : path;
    }
}
