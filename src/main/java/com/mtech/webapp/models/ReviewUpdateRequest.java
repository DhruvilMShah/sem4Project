package com.mtech.webapp.models;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewUpdateRequest {
    private String reviewId;
    private int rating;
    private String description;
    private boolean anonymity;
}
