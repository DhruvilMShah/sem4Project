package com.mtech.webapp.repositories;

import com.mtech.webapp.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WebAppRepository extends MongoRepository<User,Integer> {

    User findByName(String name);

}
