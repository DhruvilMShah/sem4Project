package com.mtech.webapp.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "achievement")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Achievement {
    @Id
    private String achievementId;
    private String email;
    private LocalDateTime created;
    private LocalDateTime lastUpdated;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String description;
    private String category;
    private List<String> evidences;
}
