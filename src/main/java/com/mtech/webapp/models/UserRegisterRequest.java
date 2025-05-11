package com.mtech.webapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class UserRegisterRequest {
    private String name;
    private String email;
    private String password;
}
