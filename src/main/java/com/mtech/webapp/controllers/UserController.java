package com.mtech.webapp.controllers;

import com.mtech.webapp.models.User;
import com.mtech.webapp.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Endpoint to fetch email suggestions
    @GetMapping("/suggestions")
    public List<String> getEmailSuggestions(@RequestParam String query) {
        List<User> users = userService.findAll();
        System.out.println("All Emails are: " + users.size());
        List<String> emailsMatching = userService.findEmailsByQuery(query);
        System.out.println("Emails matching are: " + emailsMatching);
        return emailsMatching;
    }
}

