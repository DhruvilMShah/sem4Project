package com.mtech.webapp.repositories;

import com.mtech.webapp.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByEmailContainingIgnoreCase(String query);
    List<User> findAll();

    User findByUserId(String userId);

    User findByEmail(String email);
}
