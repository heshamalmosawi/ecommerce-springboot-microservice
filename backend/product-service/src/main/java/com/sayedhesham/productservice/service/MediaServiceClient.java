package com.sayedhesham.productservice.service;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceClient {

    private final RestTemplate restTemplate;

    private final DiscoveryClient discoveryClient;

    public String getMediaBase64(String mediaId) {
        try {
            // Get media service instance from Eureka
            ServiceInstance mediaServiceInstance = discoveryClient.getInstances("mediaservice")
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Media service not available"));
            
            String url = mediaServiceInstance.getUri().toString() + "/api/media/" + mediaId;
            log.info("Fetching media from: {}", url);
            
            MediaResponse response = restTemplate.getForObject(url, MediaResponse.class);
            if (response != null && response.getBase64Data() != null) {
                return response.getBase64Data();
            }
            return null;
        } catch (RestClientException e) {
            log.error("Error fetching media with ID: {}", mediaId, e);
            return null;
        }
    }

    @Data
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