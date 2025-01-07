package com.mtech.webapp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebAppController {
    @Autowired
    private WebAppRepository webAppRepository;

    @GetMapping("/hello")
    public Users helloFromApp()
    {
        return webAppRepository.findByName("dhruv");
    }
}
