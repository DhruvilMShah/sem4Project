package com.mtech.webapp.services;

import com.mtech.webapp.models.*;
import com.mtech.webapp.repositories.UserRepository;
import com.mtech.webapp.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    UserRepository userRepository;


    public AuthResponse loginUser(LoginRequest loginRequest, Role role) {
        String userId = userRepository.findByEmail(loginRequest.getEmail()).getUserId();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, loginRequest.getPassword());
        authenticationToken.setDetails(role);
        Authentication auth = authenticationManager.authenticate(authenticationToken);
        if (auth.isAuthenticated()) {
            String token = jwtTokenUtil.generateToken(userId, role);
            return AuthResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("Logged In Successfully as " + role)
                    .email(loginRequest.getEmail())
                    .token(token)
                    .build();
        } else {
            return AuthResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid Credentials")
                    .build();
        }
    }

    public AuthResponse registerUser(UserRegisterRequest userRegisterRequest) {
        User user = new User();

        //Get from Request
        user.setName(userRegisterRequest.getName());
        user.setEmail(userRegisterRequest.getEmail());
        user.setPasswordHash(new BCryptPasswordEncoder().encode(userRegisterRequest.getPassword()));
        user.setActive(true);

        //Save to Database
        userRepository.save(user);

        //JWT Token Generation
        String token = jwtTokenUtil.generateToken(user.getUserId(), Role.USER);

        //Return Response
        return AuthResponse.builder()
                .status(HttpStatus.OK.value())
                .message("User Account created successfully with userId: " + user.getUserId())
                .token(token)
                .build();
    }

    public AuthResponse updateActivationStatus(ActivationRequest activationRequest, boolean activationStatus) {
        Role role = activationRequest.getRole();
        String userId = activationRequest.getUserId();
        if (role == Role.USER) {
            return updateStatus(userRepository.findByUserId(userId), userId, activationStatus, role.name());
        }
        else {
            return AuthResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Not a valid Role")
                    .build();
        }
    }

    private <T> AuthResponse updateStatus(T user, String userId, boolean activationStatus, String userType) {
        if (user == null) {
            return AuthResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(userType + " with ID: " + userId + " not found")
                    .build();
        }

        if (user instanceof User) {
            ((User) user).setActive(activationStatus);
            userRepository.save((User) user);
        }

        return AuthResponse.builder()
                .status(HttpStatus.OK.value())
                .message(userType + " Account with ID: " + userId + " is now " + (activationStatus ? "Activated" : "Deactivated"))
                .build();
    }


}
