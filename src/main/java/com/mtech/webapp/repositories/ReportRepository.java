package com.mtech.webapp.repositories;

import com.mtech.webapp.models.Report;
import com.mtech.webapp.models.ReportStatus;
import com.mtech.webapp.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReportRepository extends MongoRepository<Report,String> {
    List<Report> findByEmailAndStatus(String email, ReportStatus status);
}
