package com.mtech.webapp.controllers;
import com.mtech.webapp.models.User;
import com.mtech.webapp.repositories.WebAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebAppController {
    @Autowired
    private WebAppRepository webAppRepository;

    @GetMapping("/hello")
    public User helloFromApp()
    {
        return webAppRepository.findByName("dhruv");
    }
}
