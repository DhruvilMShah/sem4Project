package com.mtech.webapp.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.layout.element.Table;
import com.mtech.webapp.models.*;
import com.mtech.webapp.repositories.AchievementRepository;
import com.mtech.webapp.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
public class ReportController {
    @Autowired
    private AchievementRepository achievementRepository;
    @Autowired
    private ReportRepository reportRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String REPORTS_DIR = "reports";

    @PostMapping("/report/{userEmail}")
    @Async
    public CompletableFuture<ResponseEntity<String>>  createReportRequest(@RequestBody ReportRequest reportRequest,
                                                      @PathVariable String userEmail)
    {
        System.out.println("Creating report for " + userEmail + " of achievements between " + reportRequest.getFromDate()
                + " and " + reportRequest.getToDate() + " for framework: " + reportRequest.getFormat());
        List<Achievement> userAchievements = achievementRepository.findByEmail(userEmail);
        List<String> achievementsBetweenDuration = userAchievements.stream()
                .filter(achievement -> isBetween(achievement.getFromDate(), reportRequest.getFromDate(), reportRequest.getToDate()))
                .map(Achievement::getDescription)
                .toList();
        String pythonApiUrl = "http://localhost:5000/achievements/summarize";

        // Add callback URL so Python API can send the response later
        SummarizeReportRequest request = new SummarizeReportRequest();
        request.setCallBackUrl("http://localhost:8081/reportFormat/"+reportRequest.getFormat());
        request.setType(reportRequest.getFormat());
        request.setEmailId(userEmail);
        request.setAchievementDesc(achievementsBetweenDuration);

        restTemplate.postForEntity(pythonApiUrl, request, String.class);

        // Return immediately with HTTP 202 Accepted
        return CompletableFuture.completedFuture(ResponseEntity.accepted().body("Processing started"));
    }

    @PostMapping("/reportFormat/{type}")
    public ResponseEntity<Report> createPDFOfReport(@RequestBody ReportContent reportJSON,
                                                    @PathVariable String type) throws Exception {
        // TODO update db with status - started
        Report report = new Report();
        String emailId = reportJSON.getEmail();
        LocalDateTime requestedTime = LocalDateTime.now();
        String fileName = emailId.replace("@", "_").replace(".", "_") + "-" +
                requestedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".pdf";
        String reportFilePath = REPORTS_DIR + "/" + fileName;

        report.setEmail(emailId);
        report.setStatus(ReportStatus.IN_PROGRESS);
        report.setRequestedDate(requestedTime);
        report.setFilePath("http://localhost:8081/files/" + fileName);
        report = reportRepository.save(report);



        // Parse JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(reportJSON);
        // Parse JSON string to JsonNode
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // Extract data
        String email = jsonNode.get("email").asText();
        JsonNode achievements = jsonNode.get("ratedAchievements");

        // Create PDF
        PdfDocument pdf = new PdfDocument(new PdfWriter(reportFilePath));
        Document document = new Document(pdf);

        // Add title
        document.add(new Paragraph("Achievement Report").setBold().setFontSize(16));
        document.add(new Paragraph("Email: " + email).setFontSize(12));

        // Create table
        float[] columnWidths = {200F, 150F, 50F}; // Adjust column widths
        Table table = new Table(columnWidths);
        table.addCell("Achievement");
        table.addCell("Category");
        table.addCell("Rating");

        // Add achievements to table
        for (JsonNode achievement : achievements) {
            table.addCell(achievement.get("Achievement").asText());
            table.addCell(achievement.get("Category").asText());
            table.addCell(String.valueOf(achievement.get("Rating").asInt()));
        }

        document.add(table);
        document.close();

        System.out.println("PDF Created: " + reportFilePath);
        report.setStatus(ReportStatus.SUCCESSFUL);
        report.setCompletedDate(LocalDateTime.now());
        reportRepository.save(report);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/reports/{userEmail}")
    public ResponseEntity<List<Report>> getReports(@PathVariable String userEmail)
    {
        List<Report> allReports = reportRepository.findByEmail(userEmail);
        allReports.sort(Comparator.comparing(Report::getRequestedDate).reversed());
        return new ResponseEntity<>(allReports, HttpStatus.OK);
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(REPORTS_DIR).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    private boolean isBetween(LocalDate fromDate, LocalDate startDate, LocalDate endDate) {
        return (fromDate.isEqual(startDate) || fromDate.isAfter(startDate)) && (fromDate.isEqual(endDate) || fromDate.isBefore(endDate));
    }
}
