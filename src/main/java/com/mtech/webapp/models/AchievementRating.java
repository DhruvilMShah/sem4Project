package com.mtech.webapp.models;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AchievementRating {
    private String achievement;
    private String category;
    private int rating;
}
