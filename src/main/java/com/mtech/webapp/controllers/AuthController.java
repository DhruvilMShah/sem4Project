package com.mtech.webapp.controllers;

import com.mtech.webapp.models.Role;
import com.mtech.webapp.models.AuthResponse;
import com.mtech.webapp.models.LoginRequest;
import com.mtech.webapp.models.UserRegisterRequest;
import com.mtech.webapp.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @Tag(name = "Authentication and Authorization")
    @PostMapping("/register/user")
    @Operation(summary = "Register user", description = "Register a new user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRegisterRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"name\": \"John Doe\", \"email\": \"john.doe@example.com\", " +
                                            "\"password\": \"password123\"}}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "User successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<AuthResponse> registerUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        AuthResponse authResponse = authService.registerUser(userRegisterRequest);
        return ResponseEntity.status(authResponse.getStatus()).body(authResponse);
    }

    @Tag(name = "Authentication and Authorization")
    @PostMapping("/login/user")
    @Operation(summary = "Login user", description = "Login a user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"email\":\"abc@gmail.com\", \"password\": \"password123\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully logged in"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.loginUser(loginRequest, Role.USER);
        return ResponseEntity.status(authResponse.getStatus()).body(authResponse);
    }
}
