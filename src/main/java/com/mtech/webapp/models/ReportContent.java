package com.mtech.webapp.models;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportContent {
    private String email;
    private List<AchievementRating> ratedAchievements;
}
