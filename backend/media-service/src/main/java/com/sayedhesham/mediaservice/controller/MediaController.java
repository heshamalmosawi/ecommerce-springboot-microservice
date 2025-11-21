package com.sayedhesham.mediaservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sayedhesham.mediaservice.model.Media;
import com.sayedhesham.mediaservice.service.MediaService;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private static final Logger log = LoggerFactory.getLogger(MediaController.class);

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> uploadMedia(@RequestBody MediaUploadRequest request) {
        try {
            log.info("Received media upload request for type: {}, owner: {}",
                    request.getMediaType(), request.getOwnerId());

            Media uploadedMedia = mediaService.uploadMedia(
                    request.getBase64Data(),
                    request.getContentType(),
                    request.getMediaType(),
                    request.getOwnerId(),
                    request.getFileName()
            );

            MediaResponse response = MediaResponse.builder()
                    .id(uploadedMedia.getId())
                    .base64Data(uploadedMedia.getBase64Data())
                    .contentType(uploadedMedia.getContentType())
                    .fileSizeBytes(uploadedMedia.getFileSizeBytes())
                    .fileSizeKB(uploadedMedia.getFileSizeKB())
                    .fileSizeMB(uploadedMedia.getFileSizeMB())
                    .uploadTimestamp(uploadedMedia.getUploadTimestamp())
                    .build();

            log.info("Successfully uploaded media with ID: {}, size: {}KB",
                    uploadedMedia.getId(), uploadedMedia.getFileSizeKB());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid media upload request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error uploading media", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<MediaResponse> getMedia(@PathVariable String mediaId) {
        try {
            Media media = mediaService.getMediaById(mediaId);

            MediaResponse response = MediaResponse.builder()
                    .id(media.getId())
                    .base64Data(media.getBase64Data())
                    .contentType(media.getContentType())
                    .fileSizeBytes(media.getFileSizeBytes())
                    .fileSizeKB(media.getFileSizeKB())
                    .fileSizeMB(media.getFileSizeMB())
                    .uploadTimestamp(media.getUploadTimestamp())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error retrieving media: {}", mediaId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error retrieving media: {}", mediaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<MediaResponse>> getMediaByOwner(@PathVariable String ownerId) {
        try {
            List<Media> mediaList = mediaService.getMediaByOwner(ownerId);

            List<MediaResponse> responses = mediaList.stream()
                    .map(media -> MediaResponse.builder()
                    .id(media.getId())
                    .base64Data(media.getBase64Data())
                    .contentType(media.getContentType())
                    .fileSizeBytes(media.getFileSizeBytes())
                    .fileSizeKB(media.getFileSizeKB())
                    .fileSizeMB(media.getFileSizeMB())
                    .uploadTimestamp(media.getUploadTimestamp())
                    .build())
                    .toList();

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error retrieving media for owner: {}", ownerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String mediaId) {
        try {
            mediaService.deleteMedia(mediaId);
            log.info("Successfully deleted media: {}", mediaId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting media: {}", mediaId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error deleting media: {}", mediaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DTOs
    @Data
    public static class MediaUploadRequest {

        private String base64Data;
        private String contentType;
        private String mediaType; // "avatar", "product_image"
        private String ownerId;   // user ID or product ID
        private String fileName;   // optional filename
    }

    @Data
    @Builder
    public static class MediaResponse {

        private String id;
        private String base64Data;
        private String contentType;
        private Long fileSizeBytes;
        private Long fileSizeKB;
        private Double fileSizeMB;
        private Long uploadTimestamp;
    }
}
