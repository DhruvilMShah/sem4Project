package com.mtech.webapp.controllers;

import com.mtech.webapp.models.Achievement;
import com.mtech.webapp.repositories.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AchievementController {
    @Autowired
    private AchievementRepository achievementRepository;

    @PostMapping("/achievement")
    public ResponseEntity<Achievement> postAchievement()
    {
        Achievement achievement = new Achievement();
        achievement.setCategory("category3");
        achievement.setEmail("abc@asd.com");
        achievement.setDescription("this is a sample achievement");
        achievement.setEvidences(new ArrayList<>());
        achievement.setCreatedDate(LocalDateTime.now());
        achievement.setFromDate(LocalDateTime.now());
        achievement.setLastUpdated(LocalDateTime.now());
        achievement.setToDate(LocalDateTime.now());
        achievementRepository.save(achievement);
        return new ResponseEntity<>(achievement, HttpStatus.CREATED);
    }

    @PatchMapping("/achievement")
    public ResponseEntity<Achievement> updateAchievement()
    {
        return new ResponseEntity<>(new Achievement(), HttpStatus.CREATED);
    }

    @DeleteMapping("/achievement")
    public ResponseEntity<Achievement> deleteAchievement()
    {
        return new ResponseEntity<>(new Achievement(), HttpStatus.ACCEPTED);
    }

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<Achievement>> getAchievements()
    {
        List<Achievement> allAchievementsOfUser = achievementRepository.findByEmail("abc@qwe.com");
        return new ResponseEntity<>(allAchievementsOfUser, HttpStatus.OK);
    }

    @GetMapping("/user/{userEmail}/achievements/category/{categoryName}")
    public ResponseEntity<List<Achievement>> filterAchievementsBasedOnCategory()
    {
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
    }

    @GetMapping("/user/{userEmail}/achievements/duration/{fromDate}/{toDate}")
    public ResponseEntity<List<Achievement>> filterAchievementsBasedOnDuration()
    {
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
    }

}
