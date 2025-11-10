package com.sayedhesham.mediaservice.service;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sayedhesham.mediaservice.exception.MediaValidationException;
import com.sayedhesham.mediaservice.model.Media;
import com.sayedhesham.mediaservice.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final MediaRepository mediaRepository;

    @Value("${media.max-file-size-mb:2}")
    private Long maxFileSizeMB;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final Set<String> ALLOWED_MEDIA_TYPES = Set.of(
            "avatar",
            "product_image"
    );

    public Media uploadMedia(String base64Data, String contentType, String mediaType, String ownerId, String fileName) {
        // Validate inputs
        validateUploadRequest(base64Data, contentType, mediaType, ownerId);

        // Calculate file size
        long fileSizeBytes = calculateFileSize(base64Data);

        // Build media entity
        Media media = Media.builder()
                .id(java.util.UUID.randomUUID().toString())
                .base64Data(base64Data)
                .contentType(contentType.toLowerCase())
                .mediaType(mediaType)
                .ownerId(ownerId)
                .fileName(fileName != null ? fileName : generateFileName(contentType))
                .fileSizeBytes(fileSizeBytes)
                .uploadTimestamp(System.currentTimeMillis())
                .build();

        // Save to database
        Media savedMedia = mediaRepository.save(media);
        
        log.info("Successfully uploaded media: ID={}, type={}, owner={}, size={}KB", 
                savedMedia.getId(), mediaType, ownerId, savedMedia.getFileSizeKB());

        return savedMedia;
    }

    public Media getMediaById(String mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found: " + mediaId));
    }

    public List<Media> getMediaByOwner(String ownerId) {
        return mediaRepository.findByOwnerId(ownerId);
    }

    public void deleteMedia(String mediaId) {
        if (!mediaRepository.existsById(mediaId)) {
            throw new RuntimeException("Media not found: " + mediaId);
        }
        
        mediaRepository.deleteById(mediaId);
        log.info("Successfully deleted media: {}", mediaId);
    }

    private void validateUploadRequest(String base64Data, String contentType, String mediaType, String ownerId) {
        // Validate base64 data
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new MediaValidationException("Base64 data is required");
        }

        if (!isValidBase64(base64Data)) {
            throw new MediaValidationException("Invalid base64 data format");
        }

        // Validate content type
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new MediaValidationException("Content type is required");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new MediaValidationException("Unsupported content type: " + contentType + 
                    ". Allowed types: " + ALLOWED_CONTENT_TYPES);
        }

        // Validate media type
        if (mediaType == null || mediaType.trim().isEmpty()) {
            throw new MediaValidationException("Media type is required");
        }

        if (!ALLOWED_MEDIA_TYPES.contains(mediaType.toLowerCase())) {
            throw new MediaValidationException("Unsupported media type: " + mediaType + 
                    ". Allowed types: " + ALLOWED_MEDIA_TYPES);
        }

        // Validate owner ID
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new MediaValidationException("Owner ID is required");
        }

        // Validate file size
        long fileSizeBytes = calculateFileSize(base64Data);
        long maxSizeBytes = maxFileSizeMB * 1024 * 1024;
        
        if (fileSizeBytes > maxSizeBytes) {
            throw new MediaValidationException("File size exceeds maximum allowed size of " + 
                    maxFileSizeMB + "MB. Actual size: " + (fileSizeBytes / (1024 * 1024)) + "MB");
        }
    }

    private boolean isValidBase64(String base64Data) {
        try {
            // Remove data URL prefix if present
            String cleanBase64 = base64Data.contains(",") ? 
                    base64Data.split(",")[1] : base64Data;
            Base64.getDecoder().decode(cleanBase64);
            return true;
        } catch (Exception e) {
            try {
                // Try decoding the whole string if no comma found
                Base64.getDecoder().decode(base64Data);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    private long calculateFileSize(String base64Data) {
        // Remove data URL prefix if present
        String cleanBase64 = base64Data.contains(",") ? 
                base64Data.split(",")[1] : base64Data;
        
        // Calculate actual file size from base64
        return (cleanBase64.length() * 3) / 4;
    }

    private String generateFileName(String contentType) {
        String extension = switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        
        return "image_" + System.currentTimeMillis() + extension;
    }
}