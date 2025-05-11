package com.mtech.webapp.models;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthResponse {
    int status;
    String message;
    String email;
    String token;
}
