package com.sayedhesham.mediaservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {
    @Id
    private String id;
    
    @Field("base64_data")
    private String base64Data;
    
    @Field("content_type")
    private String contentType;
    
    @Field("file_size_bytes")
    private Long fileSizeBytes;
    
    @Field("file_name")
    private String fileName;
    
    @Field("upload_timestamp")
    private Long uploadTimestamp;
    
    @Field("media_type")
    private String mediaType; // "avatar", "product_image", etc.
    
    @Field("owner_id")
    private String ownerId; // user ID or product ID
    
    // Utility method to get file size in KB
    public Long getFileSizeKB() {
        return fileSizeBytes != null ? fileSizeBytes / 1024 : null;
    }
    
    // Utility method to get file size in MB
    public Double getFileSizeMB() {
        return fileSizeBytes != null ? (double) fileSizeBytes / (1024 * 1024) : null;
    }
}