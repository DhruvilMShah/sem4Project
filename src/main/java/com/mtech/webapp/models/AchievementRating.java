package com.mtech.webapp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AchievementRating {
    @JsonProperty("Achievement")
    private String achievement;

    @JsonProperty("Category")
    private String category;

    @JsonProperty("Rating")
    private String rating;
}
