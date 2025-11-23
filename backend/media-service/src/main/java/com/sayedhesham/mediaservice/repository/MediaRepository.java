package com.sayedhesham.mediaservice.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sayedhesham.mediaservice.model.Media;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {
    List<Media> findByOwnerId(String ownerId);
    Media findByOwnerIdAndMediaType(String ownerId, String mediaType);
    void deleteByOwnerId(String ownerId);
}