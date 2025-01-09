package com.mtech.webapp.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "report")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    @Id
    private String reportId;
    private String email;
    private ReportStatus status;
    private String filePath;
    private LocalDateTime requestedDate;
    private LocalDateTime completedDate;
}
