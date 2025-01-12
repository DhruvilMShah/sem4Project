package com.mtech.webapp.models;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AchievementUpdateRequest {
    private String achievementId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String description;
    private String category;
    private List<String> evidences;
}
