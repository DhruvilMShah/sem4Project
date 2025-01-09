package com.mtech.webapp.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


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
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdated;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String description;
    private String category;
    private List<String> evidences;
}
