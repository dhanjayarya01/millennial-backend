package com.mellinnial.plance.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map<String, Object> uploadFile(MultipartFile file) throws IOException {
        log.info("Uploading file to Cloudinary: {}", file.getOriginalFilename());
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "millennial_attachments",
                "resource_type", "auto"
        ));

        String url = (String) uploadResult.get("secure_url");
        log.info("Successfully uploaded file. Secure URL: {}", url);

        return Map.of(
                "success", true,
                "url", url,
                "name", file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"
        );
    }
}
