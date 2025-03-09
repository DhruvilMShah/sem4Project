package com.mtech.webapp.models;

import lombok.*;

import java.util.List;
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SummarizeReportRequest {
    private String emailId;
    private List<String> achievementDesc;
    private String callBackUrl;
    private String type;
}
