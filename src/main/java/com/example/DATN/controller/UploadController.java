package com.example.DATN.controller;

import com.example.DATN.service.UploadImage.UploadImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name
    ) {
        String url = uploadImageService.uploadImage(file, name);
        return ResponseEntity.status(201).body(Map.of("url", url));
    }

    @DeleteMapping(value = "/images", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteByUrl(@RequestParam("url") String url) {
        uploadImageService.deleteImage(url);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
