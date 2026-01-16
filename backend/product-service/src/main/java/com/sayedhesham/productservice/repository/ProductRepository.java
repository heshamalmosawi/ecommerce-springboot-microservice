package com.sayedhesham.productservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sayedhesham.productservice.model.Product;

public interface ProductRepository extends MongoRepository<Product, String>, ProductRepositoryCustom {
    List<Product> findByUserId(String userId);
    Page<Product> findByUserId(String userId, Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("{ 'price': { $gte: ?0, $lte: ?1 } }")
    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'price': { $gte: ?1, $lte: ?2 } }")
    Page<Product> findByNameContainingIgnoreCaseAndPriceBetween(String name, Double minPrice, Double maxPrice, Pageable pageable);

    @Query("{ 'userId': { $in: ?0 } }")
    Page<Product> findByUserIds(List<String> userIds, Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'userId': { $in: ?1 } }")
    Page<Product> findByNameContainingIgnoreCaseAndUserIds(String name, List<String> userIds, Pageable pageable);

    @Query("{ 'price': { $gte: ?0, $lte: ?1 }, 'userId': { $in: ?2 } }")
    Page<Product> findByPriceBetweenAndUserIds(Double minPrice, Double maxPrice, List<String> userIds, Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'price': { $gte: ?1, $lte: ?2 }, 'userId': { $in: ?3 } }")
    Page<Product> findByNameContainingIgnoreCaseAndPriceBetweenAndUserIds(String name, Double minPrice, Double maxPrice, List<String> userIds, Pageable pageable);
}
