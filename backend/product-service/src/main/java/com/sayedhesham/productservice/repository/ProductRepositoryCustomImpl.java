package com.sayedhesham.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.sayedhesham.productservice.model.Category;
import com.sayedhesham.productservice.model.Product;

import java.util.List;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public ProductRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Product> searchProducts(String name, Double minPrice, Double maxPrice, List<String> userIds, Category category, Pageable pageable) {
        Query query = new Query();

        if (name != null && !name.isEmpty()) {
            query.addCriteria(Criteria.where("name").regex(name, "i"));
        }

        if (minPrice != null) {
            query.addCriteria(Criteria.where("price").gte(minPrice));
        }

        if (maxPrice != null) {
            query.addCriteria(Criteria.where("price").lte(maxPrice));
        }

        if (userIds != null && !userIds.isEmpty()) {
            query.addCriteria(Criteria.where("userId").in(userIds));
        }

        if (category != null) {
            query.addCriteria(Criteria.where("category").is(category));
        }

        query.with(pageable);

        List<Product> products = mongoTemplate.find(query, Product.class);
        long total = mongoTemplate.count(query, Product.class);

        return new PageImpl<>(products, pageable, total);
    }
}
