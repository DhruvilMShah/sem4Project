package com.mtech.webapp.models;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {
    private String format;
    private LocalDate fromDate;
    private LocalDate toDate;
}
