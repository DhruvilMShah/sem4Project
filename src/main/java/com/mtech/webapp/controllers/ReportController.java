package com.mtech.webapp.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.layout.element.Table;
import com.mtech.webapp.exceptions.ResourceNotFoundException;
import com.mtech.webapp.models.*;
import com.mtech.webapp.repositories.ReportRepository;
import com.mtech.webapp.repositories.UserRepository;
import com.mtech.webapp.security.JwtTokenUtil;
import com.mtech.webapp.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;


@RestController
@SecurityRequirement(name = "jwtAuth")
public class ReportController {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportService reportService;
    private final String REPORTS_DIR = "reports";
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @PostMapping("/report/{userEmail}")
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
    public ResponseEntity<String>  createReportRequest(@RequestBody ReportRequest reportRequest,
                                                      @PathVariable @Parameter(example = "abc@gmail.com")  String userEmail)
    {
        return reportService.createReportRequestAsync(reportRequest, userEmail);
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

        logger.info("Creating report for {}", reportJSON.getEmail());

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
        logger.info("Successfully created report for {} at: {}",emailId, reportFilePath);

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
        String userId = JwtTokenUtil.getUserIdFromAuthContext();
        Role role = JwtTokenUtil.getRoleFromAuthContext();
        String senderUserEmail = userRepository.findByUserId(userId).getEmail();
        if (role.equals(Role.USER) && !senderUserEmail.equals(userEmail)) {
            throw new AccessDeniedException("You are not authorized to see reports of this user. You can only see reports for Yourself");
        }
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
            logger.warn("Marking requestId: {} as failed due to no response from report generation service"
                    , report.getReportId());
            report.setStatus(ReportStatus.FAILURE);
            report.setCompletedDate(LocalDateTime.now());
            reportRepository.save(report);
        }
    }

}
