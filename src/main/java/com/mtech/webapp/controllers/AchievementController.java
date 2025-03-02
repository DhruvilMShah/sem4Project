package com.mtech.webapp.controllers;

import com.mtech.webapp.models.Achievement;
import com.mtech.webapp.models.AchievementDeleteRequest;
import com.mtech.webapp.models.AchievementRequest;
import com.mtech.webapp.models.AchievementUpdateRequest;
import com.mtech.webapp.repositories.AchievementRepository;
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
    public ResponseEntity<Achievement> updateAchievement(@RequestBody AchievementUpdateRequest achievementRequest)
    {
        Achievement existingAchievement = achievementRepository.findByAchievementId(achievementRequest.getAchievementId());
        // TODO add check only email logged in can update achievement of the email in achievement.
        existingAchievement.setLastUpdated(LocalDateTime.now());
        existingAchievement.setCategory(achievementRequest.getCategory());
        existingAchievement.setLastUpdated(LocalDateTime.now());
        existingAchievement.setFromDate(achievementRequest.getFromDate());
        existingAchievement.setDescription(achievementRequest.getDescription());
        existingAchievement.setEvidences(achievementRequest.getEvidences());
        achievementRepository.save(existingAchievement);
        return new ResponseEntity<>(existingAchievement, HttpStatus.CREATED);
    }

    @DeleteMapping("/achievement")
    public ResponseEntity<Achievement> deleteAchievement(@RequestBody AchievementDeleteRequest achievementDeleteRequest)
    {
        System.out.println("Deleting achievement id: "+ achievementDeleteRequest.getAchievementId());
        int noOfAchievementsDeleted = achievementRepository.deleteByAchievementId(achievementDeleteRequest.getAchievementId());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<Achievement>> getAchievements(@PathVariable String userEmail)
    {
        System.out.println("Getting achievements of : "+ userEmail);
        List<Achievement> allAchievementsOfUser = achievementRepository.findByEmail(userEmail);
        allAchievementsOfUser.sort(Comparator.comparing(Achievement::getFromDate).reversed());
        return new ResponseEntity<>(allAchievementsOfUser, HttpStatus.OK);
    }

    @GetMapping("/user/{userEmail}/achievements/category/{categoryName}")
    public ResponseEntity<List<Achievement>> filterAchievementsBasedOnCategory(@PathVariable String userEmail,
                                                                               @PathVariable String categoryName)
    {
        List<Achievement> filteredByCategory = achievementRepository.findByEmailAndCategory(userEmail, categoryName);
        return new ResponseEntity<>(filteredByCategory, HttpStatus.OK);
    }

    @GetMapping("/user/{userEmail}/achievements/duration/{startDate}/{endDate}")
    public ResponseEntity<List<Achievement>> filterAchievementsBasedOnDuration(@PathVariable String userEmail,
                                                                               @PathVariable LocalDate startDate,
                                                                               @PathVariable LocalDate endDate)
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
