package com.sayedhesham.productservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sayedhesham.productservice.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<User> findByNameContainingIgnoreCase(String name);
}
