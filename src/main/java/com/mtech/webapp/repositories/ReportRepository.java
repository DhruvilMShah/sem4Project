package com.mtech.webapp.repositories;

import com.mtech.webapp.models.Report;
import com.mtech.webapp.models.ReportStatus;
import com.mtech.webapp.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends MongoRepository<Report,String> {
    List<Report> findByEmail(String email);
    Report findByReportId(String reportId);

    List<Report> findByStatusAndRequestedDateBefore(ReportStatus reportStatus, LocalDateTime thresholdTime);
}
