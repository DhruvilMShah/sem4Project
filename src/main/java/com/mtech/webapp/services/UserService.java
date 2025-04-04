package com.mtech.webapp.services;

import com.mtech.webapp.models.User;
import com.mtech.webapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<String> findEmailsByQuery(String query) {
        return userRepository.findByEmailContainingIgnoreCase(query)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }
}

