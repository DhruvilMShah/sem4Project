package com.mtech.webapp.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.layout.element.Table;
import com.mtech.webapp.exceptions.ResourceNotFoundException;
import com.mtech.webapp.models.*;
import com.mtech.webapp.repositories.AchievementRepository;
import com.mtech.webapp.repositories.ReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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
    @Tag(name = "Evaluation Report")
    @Operation(summary = "Request evaluation report generation", description = "Starts report generation process by " +
            "requesting for mapping capabilities to evaluation framework",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Evaluation Report Request Payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReportRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"format\": \"ECDF\", \"fromDate\": \"2025-01-12\", \"toDate\": \"2025-03-12\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Report generation process started successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public CompletableFuture<ResponseEntity<String>>  createReportRequest(@RequestBody ReportRequest reportRequest,
                                                      @PathVariable @Parameter(example = "abc@gmail.com")  String userEmail)
    {
        System.out.println("Creating report request for " + userEmail + " of achievements between " + reportRequest.getFromDate()
                + " and " + reportRequest.getToDate() + " for framework: " + reportRequest.getFormat());
        List<Achievement> userAchievements = achievementRepository.findByEmail(userEmail);
        if (userAchievements == null) {
            throw new ResourceNotFoundException("userAchievements not found with userEmail: " + userEmail);
        }
        List<String> achievementsBetweenDuration = userAchievements.stream()
                .filter(achievement -> isBetween(achievement.getFromDate(), reportRequest.getFromDate(), reportRequest.getToDate()))
                .map(Achievement::getDescription)
                .toList();
        String pythonApiUrl = "http://localhost:5000/achievements/summarize";

        String reportId = UUID.randomUUID().toString();
        // Create initial Report with IN_PROGRESS
        Report report = new Report();
        report.setReportId(reportId);
        report.setEmail(userEmail);
        report.setStatus(ReportStatus.IN_PROGRESS);
        report.setRequestedDate(LocalDateTime.now());
        reportRepository.save(report);

        // Add callback URL so Python API can send the response later
        SummarizeReportRequest request = new SummarizeReportRequest();
        request.setReportId(reportId);
        request.setCallBackUrl("http://localhost:8081/reportFormat/"+reportRequest.getFormat());
        request.setType(reportRequest.getFormat());
        request.setEmailId(userEmail);
        request.setAchievementDesc(achievementsBetweenDuration);

        try {
            // Attempt to send request to Python server
            restTemplate.postForEntity(pythonApiUrl, request, String.class);
            System.out.println("Creating report request for " + userEmail + " is accepted.");
            // Return HTTP 202 Accepted if successful
            return CompletableFuture.completedFuture(
                    ResponseEntity.accepted().body("Processing started"));
        } catch (RestClientException e) {
            // Handle case where Python server is down or unreachable
            System.err.println("Error communicating with Python server: " + e.getMessage());
            System.out.println("Creating report request for " + userEmail + " is NOT accepted.");
            report.setEmail(userEmail);
            report.setStatus(ReportStatus.FAILURE);
            report.setCompletedDate(LocalDateTime.now());
            reportRepository.save(report);
            // Return HTTP 503 Service Unavailable or another appropriate response
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body("Python server is currently unavailable. Please try again later."));
        } catch (Exception e) {
            // Handle other unexpected errors
            System.err.println("Unexpected error: " + e.getMessage());
            System.out.println("Creating report request for " + userEmail + " is NOT accepted.");
            report.setEmail(userEmail);
            report.setStatus(ReportStatus.FAILURE);
            report.setCompletedDate(LocalDateTime.now());
            reportRepository.save(report);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("An unexpected error occurred."));
        }
    }

    @PostMapping("/reportFormat/{type}")
    @Tag(name = "Evaluation Report")
    @Operation(summary = "Create PDF report", description = "This callback API will create PDF report based on report content it gets in JSON",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Create Report Payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReportContent.class),
                            examples = @ExampleObject(
                                    value = "{ \"email\": \"abc@gmail.com\", " +
                                            " \"ratedAchievements\": [{\"achievement\": \"Developed a platform which has 800+ active users\"," +
                                            "\"category\": \"Innovation\",\"rating\":\"4\"}" +
                                            "]}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Report created successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<Report> createPDFOfReport(@RequestBody ReportContent reportJSON,
                                                    @PathVariable String type) throws Exception {
        Report report = new Report();
        String emailId = reportJSON.getEmail();
        LocalDateTime requestedTime = LocalDateTime.now();
        String fileName = emailId.replace("@", "_").replace(".", "_") + "-" +
                requestedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".pdf";
        String reportFilePath = REPORTS_DIR + "/" + fileName;

        System.out.println("Creating report for " + reportJSON.getEmail());

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

        report = reportRepository.findByReportId(reportJSON.getReportId());
        report.setStatus(ReportStatus.SUCCESSFUL);
        report.setCompletedDate(LocalDateTime.now());
        report.setFilePath("http://localhost:8081/files/" + fileName);
        reportRepository.save(report);
        System.out.println("Successfully created report for " + emailId + " at: " + reportFilePath);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/reports/{userEmail}")
    @Tag(name = "Evaluation Report")
    @Operation(summary = "Get all reports for given user", description = "Retrieve all reports requested by user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved all report requests"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<List<Report>> getReports(@PathVariable @Parameter(example = "abc@gmail.com")  String userEmail)
    {
        List<Report> allReports = reportRepository.findByEmail(userEmail);
        if (allReports == null) {
            throw new ResourceNotFoundException("Reports not found for userEmail: " + userEmail);
        }
        allReports.sort(Comparator.comparing(Report::getRequestedDate).reversed());
        return new ResponseEntity<>(allReports, HttpStatus.OK);
    }

    @GetMapping("/files/{filename}")
    @Tag(name = "Evaluation Report")
    @Operation(summary = "Get given PDF report", description = "Retrieves given report PDF file",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved PDF report"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<Resource> serveFile(@PathVariable @Parameter(example = "report.pdf")  String filename) throws IOException {
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

    @Scheduled(fixedRate = 60000) // every 1 min
    public void checkForStaleReports() {
        // Calculate 1 minute ago
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(1);

        // Fetch all reports that are still IN_PROGRESS and were requested more than 15 minutes ago
        List<Report> stuckReports = reportRepository.findByStatusAndRequestedDateBefore(
                ReportStatus.IN_PROGRESS, thresholdTime
        );

        for (Report report : stuckReports) {
            System.out.println("Marking requestId: " + report.getReportId() + " as failed due to no response from " +
                    "report generation service");
            report.setStatus(ReportStatus.FAILURE);
            report.setCompletedDate(LocalDateTime.now());
            reportRepository.save(report);
        }
    }

}
