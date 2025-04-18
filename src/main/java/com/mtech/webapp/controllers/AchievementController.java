package com.mtech.webapp.controllers;

import com.mtech.webapp.models.*;
import com.mtech.webapp.repositories.AchievementRepository;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
public class AchievementController {
    @Autowired
    private AchievementRepository achievementRepository;

    @PostMapping("/achievement")
    @Tag(name = "User Achievements")
    @Operation(summary = "Add an achievement", description = "Add achievement for given user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Add Achievement Payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AchievementRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"email\": \"abc@gmail.com\", \"fromDate\": 2024-08-10, " +
                                            "\"toDate\": \"2025-02-10\", \"description\": \"Improved platform performance by 25%\", \"category\": \"Performance\"," +
                                            "\"evidences\": [\"http://evidenceIdid.html\", \"http://evidenceIActuallydid.html\", \"http://evidenceIReallydid.html\"] }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Achievement added successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<Achievement> postAchievement(@RequestBody AchievementRequest achievementRequest)
    {
        System.out.println("Adding achievement for : "+ achievementRequest.getEmail());
        Achievement achievement = new Achievement();
        achievement.setCategory(achievementRequest.getCategory());
        achievement.setEmail(achievementRequest.getEmail());
        achievement.setDescription(achievementRequest.getDescription());
        achievement.setEvidences(achievementRequest.getEvidences());
        achievement.setCreated(LocalDateTime.now());
        achievement.setLastUpdated(LocalDateTime.now());
        achievement.setFromDate(achievementRequest.getFromDate());
        achievement.setToDate(achievementRequest.getToDate());
        achievementRepository.save(achievement);
        return new ResponseEntity<>(achievement, HttpStatus.CREATED);
    }

    @PatchMapping("/achievement")
    @Tag(name = "User Achievements")
    @Operation(summary = "Patch an achievement", description = "Update an existing achievement",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Update Achievement Payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AchievementUpdateRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"achievementId\": \"a123wert5u67\", \"fromDate\": 2024-08-10, " +
                                            "\"toDate\": \"2025-02-10\", \"description\": \"Improved platform performance by 25%\", \"category\": \"Performance\"," +
                                            "\"evidences\": [\"http://evidenceIdid.html\", \"http://evidenceIActuallydid.html\", \"http://evidenceIReallydid.html\"] }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Achievement updated successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<Achievement> updateAchievement(@RequestBody AchievementUpdateRequest achievementRequest)
    {
        Achievement existingAchievement = achievementRepository.findByAchievementId(achievementRequest.getAchievementId());
        // TODO add check only email logged in can update achievement of the email in achievement.
        existingAchievement.setLastUpdated(LocalDateTime.now());
        existingAchievement.setCategory(achievementRequest.getCategory());
        existingAchievement.setToDate(achievementRequest.getToDate());
        existingAchievement.setFromDate(achievementRequest.getFromDate());
        existingAchievement.setDescription(achievementRequest.getDescription());
        existingAchievement.setEvidences(achievementRequest.getEvidences());
        achievementRepository.save(existingAchievement);
        return new ResponseEntity<>(existingAchievement, HttpStatus.CREATED);
    }

    @DeleteMapping("/achievement")
    @Tag(name = "User Achievements")
    @Operation(summary = "Delete an achievement", description = "Delete an existing achievement",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Delete Achievement Payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AchievementDeleteRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"achievementId\": \"a102gth356\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Achievement deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<Achievement> deleteAchievement(@RequestBody AchievementDeleteRequest achievementDeleteRequest)
    {
        System.out.println("Deleting achievement id: "+ achievementDeleteRequest.getAchievementId());
        int noOfAchievementsDeleted = achievementRepository.deleteByAchievementId(achievementDeleteRequest.getAchievementId());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/user/{userEmail}")
    @Tag(name = "User Achievements")
    @Operation(summary = "Get all user achievements", description = "Retrieve all achievements of a given user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved all user achievements"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<List<Achievement>> getAchievements(@PathVariable @Parameter(example = "abc@gmail.com") String userEmail)
    {
        System.out.println("Getting achievements of : "+ userEmail);
        List<Achievement> allAchievementsOfUser = achievementRepository.findByEmail(userEmail);
        allAchievementsOfUser.sort(Comparator.comparing(Achievement::getFromDate).reversed());
        return new ResponseEntity<>(allAchievementsOfUser, HttpStatus.OK);
    }

    @GetMapping("/user/{userEmail}/achievements/category/{categoryName}")
    @Tag(name = "User Achievements")
    @Operation(summary = "Get all user achievements of certain category", description = "Retrieve all achievements of a given user in a given category",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved such user achievements"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<List<Achievement>> filterAchievementsBasedOnCategory(@PathVariable @Parameter(example = "abc@gmail.com") String userEmail,
                                                                               @PathVariable @Parameter(example = "Technical") String categoryName)
    {
        List<Achievement> filteredByCategory = achievementRepository.findByEmailAndCategory(userEmail, categoryName);
        return new ResponseEntity<>(filteredByCategory, HttpStatus.OK);
    }

    @GetMapping("/user/{userEmail}/achievements/duration/{startDate}/{endDate}")
    @Tag(name = "User Achievements")
    @Operation(summary = "Get all user achievements in given time interval", description = "Retrieve all achievements of a given user in a given time interval",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved such user achievements"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<List<Achievement>> filterAchievementsBasedOnDuration(@PathVariable @Parameter(example = "abc@gmail.com") String userEmail,
                                                                               @PathVariable @Parameter(example = "2024-01-17") LocalDate startDate,
                                                                               @PathVariable @Parameter(example = "2024-07-31") LocalDate endDate)
    {
        List<Achievement> userAchievements = achievementRepository.findByEmail(userEmail);
        List<Achievement> achievementsBetweenDuration = userAchievements.stream()
                .filter(achievement -> isBetween(achievement.getFromDate(), startDate, endDate))
                .toList();
        return new ResponseEntity<>(achievementsBetweenDuration, HttpStatus.OK);
    }

    private boolean isBetween(LocalDate fromDate, LocalDate startDate, LocalDate endDate) {
        return (fromDate.isEqual(startDate) || fromDate.isAfter(startDate)) && (fromDate.isEqual(endDate) || fromDate.isBefore(endDate));
    }

}
