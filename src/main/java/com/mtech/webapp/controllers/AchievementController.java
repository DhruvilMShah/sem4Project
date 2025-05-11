package com.mtech.webapp.controllers;

import com.mtech.webapp.exceptions.ResourceNotFoundException;
import com.mtech.webapp.models.*;
import com.mtech.webapp.repositories.AchievementRepository;
import com.mtech.webapp.repositories.UserRepository;
import com.mtech.webapp.security.JwtTokenUtil;
import com.mtech.webapp.security.WebSecurityConfig;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@SecurityRequirement(name = "jwtAuth")
public class AchievementController {
    @Autowired
    private AchievementRepository achievementRepository;
    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(AchievementController.class);

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
        String userId = JwtTokenUtil.getUserIdFromAuthContext();
        Role role = JwtTokenUtil.getRoleFromAuthContext();
        String senderUserEmail = userRepository.findByUserId(userId).getEmail();
        if (role.equals(Role.USER) && !senderUserEmail.equals(achievementRequest.getEmail())) {
            throw new AccessDeniedException("You are not authorized to create this resource. You can only add achievement for Yourself");
        }

        logger.info("Adding achievement for : {}", achievementRequest.getEmail());
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
        logger.info("Successfully added achievement for : {}",achievementRequest.getEmail());
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
        String userId = JwtTokenUtil.getUserIdFromAuthContext();
        Role role = JwtTokenUtil.getRoleFromAuthContext();
        String senderUserEmail = userRepository.findByUserId(userId).getEmail();
        String achievementGivenByUserEmail = achievementRepository.findByAchievementId(achievementRequest.getAchievementId()).getEmail();
        if (role.equals(Role.USER) && !senderUserEmail.equals(achievementGivenByUserEmail)) {
            throw new AccessDeniedException("You are not authorized to update this resource. You can only update achievement for Yourself");
        }
        Achievement existingAchievement = achievementRepository.findByAchievementId(achievementRequest.getAchievementId());
        if (existingAchievement == null) {
            throw new ResourceNotFoundException("No achievement exists for given achievement id: " + achievementRequest.getAchievementId());
        }
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
        String userId = JwtTokenUtil.getUserIdFromAuthContext();
        Role role = JwtTokenUtil.getRoleFromAuthContext();
        String senderUserEmail = userRepository.findByUserId(userId).getEmail();
        String achievementGivenByUserEmail = achievementRepository.findByAchievementId(achievementDeleteRequest.getAchievementId()).getEmail();
        if (role.equals(Role.USER) && !senderUserEmail.equals(achievementGivenByUserEmail)) {
            throw new AccessDeniedException("You are not authorized to delete this resource. You can only delete achievement for Yourself");
        }
        logger.info("Deleting achievement id: {}", achievementDeleteRequest.getAchievementId());
        int noOfAchievementsDeleted = achievementRepository.deleteByAchievementId(achievementDeleteRequest.getAchievementId());
        logger.info("Successfully deleted achievement id: {}", achievementDeleteRequest.getAchievementId());
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
        logger.debug("Getting achievements of : {}", userEmail);
        List<Achievement> allAchievementsOfUser = achievementRepository.findByEmail(userEmail);
        if (allAchievementsOfUser == null) {
            throw new ResourceNotFoundException("No achievement exists for given user email: " + userEmail);
        }
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
        if (filteredByCategory == null) {
            throw new ResourceNotFoundException("No achievement exists for given user email " + userEmail + " and category: " + categoryName);
        }
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
        if (userAchievements == null) {
            throw new ResourceNotFoundException("No achievement exists for given user email " + userEmail);
        }
        List<Achievement> achievementsBetweenDuration = userAchievements.stream()
                .filter(achievement -> isBetween(achievement.getFromDate(), startDate, endDate))
                .toList();
        return new ResponseEntity<>(achievementsBetweenDuration, HttpStatus.OK);
    }

    private boolean isBetween(LocalDate fromDate, LocalDate startDate, LocalDate endDate) {
        return (fromDate.isEqual(startDate) || fromDate.isAfter(startDate)) && (fromDate.isEqual(endDate) || fromDate.isBefore(endDate));
    }

}
