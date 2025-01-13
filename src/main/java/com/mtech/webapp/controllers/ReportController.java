package com.mtech.webapp.controllers;

import com.mtech.webapp.models.*;
import com.mtech.webapp.repositories.AchievementRepository;
import com.mtech.webapp.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ReportController {
    @Autowired
    private AchievementRepository achievementRepository;

    @PostMapping("/report/{userEmail}")
    public ResponseEntity<Review> createReportRequest(@RequestBody ReportRequest reportRequest,
                                                      @PathVariable String userEmail)
    {
        System.out.println("Creating report for " + userEmail + " of achievements between " + reportRequest.getFromDate()
                + " and " + reportRequest.getToDate() + "for framework: " + reportRequest.getFormat());
        List<Achievement> userAchievements = achievementRepository.findByEmail(userEmail);
        // TODO filter by from and to date
        // TODO call python app
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/reportFormat/{type}")
    public ResponseEntity<Review> createPDFOfReport(@RequestBody ReportContent reportJSON,
                                                    @PathVariable String type)
    {
        // TODO add impl
        // update db with status - started
        // createpdf
        // save pdf
        // update db with status - completed

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/reports/${userEmail}")
    public ResponseEntity<List<Review>> getReports(@PathVariable String userEmail)
    {
        // TODO add impl
        // get reports for user which are completed
        // Create objects
        // Return JSON
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
