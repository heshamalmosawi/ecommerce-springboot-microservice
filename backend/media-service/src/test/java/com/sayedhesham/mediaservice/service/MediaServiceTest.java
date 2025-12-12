package com.sayedhesham.mediaservice.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sayedhesham.mediaservice.exception.MediaValidationException;
import com.sayedhesham.mediaservice.model.Media;
import com.sayedhesham.mediaservice.repository.MediaRepository;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private MediaService mediaService;

    private Media testMedia;
    private String validBase64Data;
    private String validContentType;
    private String validMediaType;
    private String validOwnerId;
    private String validFileName;

    @BeforeEach
    void setUp() {
        validBase64Data = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAhEAABAwEBAQEBAQEBAQAAAAAAAAABAgMEBQYHCAkKCwEAAwEAAAAAAAAAAAAAAAAAAAAA";
        validContentType = "image/jpeg";
        validMediaType = "avatar";
        validOwnerId = "user123";
        validFileName = "test.jpg";

        testMedia = Media.builder()
                .id("media1")
                .base64Data(validBase64Data)
                .contentType(validContentType)
                .mediaType(validMediaType)
                .ownerId(validOwnerId)
                .fileName(validFileName)
                .fileSizeBytes(1024L)
                .uploadTimestamp(System.currentTimeMillis())
                .build();

        ReflectionTestUtils.setField(mediaService, "maxFileSizeMB", 2L);
    }

    @Test
    void uploadMedia_WithValidData_ShouldSaveMedia() {
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);

        Media result = mediaService.uploadMedia(validBase64Data, validContentType, validMediaType, validOwnerId, validFileName);

        assertNotNull(result);
        assertEquals("media1", result.getId());
        assertEquals(validBase64Data, result.getBase64Data());
        assertEquals(validContentType, result.getContentType());
        assertEquals(validMediaType, result.getMediaType());
        assertEquals(validOwnerId, result.getOwnerId());
        assertEquals(validFileName, result.getFileName());
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void uploadMedia_WithNullBase64Data_ShouldThrowException() {
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(null, validContentType, validMediaType, validOwnerId, validFileName));
        
        assertEquals("Base64 data is required", exception.getMessage());
    }

    @Test
    void uploadMedia_WithEmptyBase64Data_ShouldThrowException() {
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia("", validContentType, validMediaType, validOwnerId, validFileName));
        
        assertEquals("Base64 data is required", exception.getMessage());
    }

    @Test
    void uploadMedia_WithInvalidBase64Data_ShouldThrowException() {
        String invalidBase64 = "invalid-base64-data";
        
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(invalidBase64, validContentType, validMediaType, validOwnerId, validFileName));
        
        assertEquals("Invalid base64 data format", exception.getMessage());
    }

    @Test
    void uploadMedia_WithNullContentType_ShouldThrowException() {
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(validBase64Data, null, validMediaType, validOwnerId, validFileName));
        
        assertEquals("Content type is required", exception.getMessage());
    }

    @Test
    void uploadMedia_WithEmptyContentType_ShouldThrowException() {
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(validBase64Data, "", validMediaType, validOwnerId, validFileName));
        
        assertEquals("Content type is required", exception.getMessage());
    }

    @Test
    void uploadMedia_WithUnsupportedContentType_ShouldThrowException() {
        String unsupportedContentType = "application/pdf";
        
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(validBase64Data, unsupportedContentType, validMediaType, validOwnerId, validFileName));
        
        assertTrue(exception.getMessage().contains("Unsupported content type"));
        assertTrue(exception.getMessage().contains("application/pdf"));
    }

    @Test
    void uploadMedia_WithNullMediaType_ShouldThrowException() {
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(validBase64Data, validContentType, null, validOwnerId, validFileName));
        
        assertEquals("Media type is required", exception.getMessage());
    }

    @Test
    void uploadMedia_WithEmptyMediaType_ShouldThrowException() {
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(validBase64Data, validContentType, "", validOwnerId, validFileName));
        
        assertEquals("Media type is required", exception.getMessage());
    }

    @Test
    void uploadMedia_WithUnsupportedMediaType_ShouldThrowException() {
        String unsupportedMediaType = "video";
        
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(validBase64Data, validContentType, unsupportedMediaType, validOwnerId, validFileName));
        
        assertTrue(exception.getMessage().contains("Unsupported media type"));
        assertTrue(exception.getMessage().contains("video"));
    }

    @Test
    void uploadMedia_WithNullOwnerId_ShouldThrowException() {
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(validBase64Data, validContentType, validMediaType, null, validFileName));
        
        assertEquals("Owner ID is required", exception.getMessage());
    }

    @Test
    void uploadMedia_WithEmptyOwnerId_ShouldThrowException() {
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(validBase64Data, validContentType, validMediaType, "", validFileName));
        
        assertEquals("Owner ID is required", exception.getMessage());
    }

    @Test
    void uploadMedia_WithOversizedFile_ShouldThrowException() {
        String largeBase64Data = createLargeBase64Data(3 * 1024 * 1024);
        
        MediaValidationException exception = assertThrows(MediaValidationException.class, 
                () -> mediaService.uploadMedia(largeBase64Data, validContentType, validMediaType, validOwnerId, validFileName));
        
        assertTrue(exception.getMessage().contains("File size exceeds maximum allowed size of 2MB"));
    }

    @Test
    void uploadMedia_WithValidImageContentTypes_ShouldSaveMedia() {
        List<String> validContentTypes = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");
        
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);

        for (String contentType : validContentTypes) {
            Media result = mediaService.uploadMedia(validBase64Data, contentType, validMediaType, validOwnerId, validFileName);
            assertNotNull(result);
        }
        
        verify(mediaRepository, times(5)).save(any(Media.class));
    }

    @Test
    void uploadMedia_WithValidMediaTypes_ShouldSaveMedia() {
        List<String> validMediaTypes = Arrays.asList("avatar", "product_image");
        
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);

        for (String mediaType : validMediaTypes) {
            Media result = mediaService.uploadMedia(validBase64Data, validContentType, mediaType, validOwnerId, validFileName);
            assertNotNull(result);
        }
        
        verify(mediaRepository, times(2)).save(any(Media.class));
    }

    @Test
    void uploadMedia_WithoutFileName_ShouldGenerateFileName() {
        Media generatedFileNameMedia = Media.builder()
                .id("media1")
                .base64Data(validBase64Data)
                .contentType(validContentType)
                .mediaType(validMediaType)
                .ownerId(validOwnerId)
                .fileName("image_" + System.currentTimeMillis() + ".jpg")
                .fileSizeBytes(1024L)
                .uploadTimestamp(System.currentTimeMillis())
                .build();

        when(mediaRepository.save(any(Media.class))).thenReturn(generatedFileNameMedia);

        Media result = mediaService.uploadMedia(validBase64Data, validContentType, validMediaType, validOwnerId, null);

        assertNotNull(result);
        assertTrue(result.getFileName().endsWith(".jpg"));
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void getMediaById_WhenMediaExists_ShouldReturnMedia() {
        when(mediaRepository.findById("media1")).thenReturn(Optional.of(testMedia));

        Media result = mediaService.getMediaById("media1");

        assertNotNull(result);
        assertEquals("media1", result.getId());
        assertEquals(validBase64Data, result.getBase64Data());
    }

    @Test
    void getMediaById_WhenMediaNotExists_ShouldThrowException() {
        when(mediaRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> mediaService.getMediaById("nonexistent"));
        
        assertEquals("Media not found: nonexistent", exception.getMessage());
    }

    @Test
    void getMediaByOwner_ShouldReturnListOfMedia() {
        List<Media> mediaList = Arrays.asList(
                Media.builder().id("media1").ownerId(validOwnerId).build(),
                Media.builder().id("media2").ownerId(validOwnerId).build()
        );

        when(mediaRepository.findByOwnerId(validOwnerId)).thenReturn(mediaList);

        List<Media> result = mediaService.getMediaByOwner(validOwnerId);

        assertEquals(2, result.size());
        assertEquals("media1", result.get(0).getId());
        assertEquals("media2", result.get(1).getId());
    }

    @Test
    void deleteMedia_WhenMediaExists_ShouldDeleteMedia() {
        when(mediaRepository.existsById("media1")).thenReturn(true);

        mediaService.deleteMedia("media1");

        verify(mediaRepository).deleteById("media1");
    }

    @Test
    void deleteMedia_WhenMediaNotExists_ShouldThrowException() {
        when(mediaRepository.existsById("nonexistent")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> mediaService.deleteMedia("nonexistent"));
        
        assertEquals("Media not found: nonexistent", exception.getMessage());
    }

    private String createLargeBase64Data(int sizeInBytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("A");
        }
        String base64Chunk = sb.toString();
        int chunksNeeded = (sizeInBytes * 4 / 3) / 1000 + 1;
        
        for (int i = 0; i < chunksNeeded; i++) {
            sb.append(base64Chunk);
        }
        
        return "data:image/jpeg;base64," + sb.toString();
    }
}