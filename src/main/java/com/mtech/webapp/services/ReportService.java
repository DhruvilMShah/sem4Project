package com.mtech.webapp.services;

import com.mtech.webapp.controllers.UserController;
import com.mtech.webapp.exceptions.ResourceNotFoundException;
import com.mtech.webapp.models.*;
import com.mtech.webapp.repositories.AchievementRepository;
import com.mtech.webapp.repositories.ReportRepository;
import com.mtech.webapp.repositories.UserRepository;
import com.mtech.webapp.security.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);


    public ResponseEntity<String> createReportRequestAsync(ReportRequest reportRequest, String userEmail) {
        String userId = JwtTokenUtil.getUserIdFromAuthContext();
        Role role = JwtTokenUtil.getRoleFromAuthContext();
        String senderUserEmail = userRepository.findByUserId(userId).getEmail();
        if (role.equals(Role.USER) && !senderUserEmail.equals(userEmail)) {
            throw new AccessDeniedException("You are not authorized to request for report of this user. You can only request report for Yourself");
        }

        logger.info("Creating report request for {} of achievements between {} and {} for framework: {}",
                userEmail, reportRequest.getFromDate(), reportRequest.getToDate(), reportRequest.getFormat());

        // Ensure we don't get null values here
        List<Achievement> userAchievements = achievementRepository.findByEmail(userEmail);
        if (userAchievements == null || userAchievements.isEmpty()) {
            throw new ResourceNotFoundException("userAchievements not found with userEmail: " + userEmail);
        }

        List<String> achievementsBetweenDuration = userAchievements.stream()
                .filter(achievement -> isBetween(achievement.getFromDate(), reportRequest.getFromDate(), reportRequest.getToDate()))
                .map(Achievement::getDescription)
                .collect(Collectors.toList());

        String pythonApiUrl = "http://localhost:5000/achievements/summarize";

        String reportId = UUID.randomUUID().toString();
        Report report = new Report();
        report.setReportId(reportId);
        report.setEmail(userEmail);
        report.setStatus(ReportStatus.IN_PROGRESS);
        report.setRequestedDate(LocalDateTime.now());
        reportRepository.save(report);

        SummarizeReportRequest request = new SummarizeReportRequest();
        request.setReportId(reportId);
        request.setCallBackUrl("http://localhost:8081/reportFormat/" + reportRequest.getFormat());
        request.setType(reportRequest.getFormat());
        request.setEmailId(userEmail);
        request.setAchievementDesc(achievementsBetweenDuration);

        try {
            restTemplate.postForEntity(pythonApiUrl, request, String.class);
            logger.info("Creating report request for {} is accepted.", userEmail);
            return ResponseEntity.accepted().body("Processing started");
        } catch (RestClientException e) {
            handleError(e, report, userEmail);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Python server is currently unavailable. Please try again later.");
        } catch (Exception e) {
            handleError(e, report, userEmail);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.");
        }
    }

    private void handleError(Exception e, Report report, String userEmail) {
        logger.error("Error: {}", e.getMessage());
        report.setEmail(userEmail);
        report.setStatus(ReportStatus.FAILURE);
        report.setCompletedDate(LocalDateTime.now());
        reportRepository.save(report);
    }

    private boolean isBetween(LocalDate fromDate, LocalDate startDate, LocalDate endDate) {
        return (fromDate.isEqual(startDate) || fromDate.isAfter(startDate)) && (fromDate.isEqual(endDate) || fromDate.isBefore(endDate));
    }
}



