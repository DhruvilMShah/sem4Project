package com.mtech.webapp.controllers;

import com.mtech.webapp.services.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "jwtAuth")
public class UserController {

    @Autowired
    private UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // Endpoint to fetch email suggestions
    @GetMapping("/suggestions")
    @Tag(name = "Email Suggestions")
    @Operation(summary = "Get all email suggestions matching entered letters", description = "Retrieve a list of all matching user emails",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of emails matching entered letters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public List<String> getEmailSuggestions(@RequestParam @Parameter(example = "abc") String query) {
        List<String> emailsMatching = userService.findEmailsByQuery(query);
        logger.debug("Emails matching are: {}", emailsMatching);
        return emailsMatching;
    }
}

