package com.mtech.webapp;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface WebAppRepository extends MongoRepository<Users,Integer> {

    Users findByName(String name);

}
