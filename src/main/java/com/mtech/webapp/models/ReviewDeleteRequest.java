package com.mtech.webapp.models;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDeleteRequest {
    private String reviewId;
}
