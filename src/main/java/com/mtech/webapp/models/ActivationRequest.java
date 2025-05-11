package com.mtech.webapp.models;


import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivationRequest {
    private Role role;
    private String userId;
}
