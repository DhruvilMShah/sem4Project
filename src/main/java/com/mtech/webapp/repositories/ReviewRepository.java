package com.mtech.webapp.repositories;

import com.mtech.webapp.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review,String> {
    List<Review> findByEmail(String email);
}
