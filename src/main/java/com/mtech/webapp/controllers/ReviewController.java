package com.mtech.webapp.controllers;

import com.mtech.webapp.models.Achievement;
import com.mtech.webapp.models.Review;
import com.mtech.webapp.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewController {
    @Autowired
    private ReviewRepository reviewRepository;

    @PostMapping("/review")
    public ResponseEntity<Review> postReview()
    {
        Review review = new Review();
        review.setAnonymity(false);
        review.setDescription("this is a sample reveiw");
        review.setRating(4);
        review.setEmail("abc@asd.com");
        reviewRepository.save(review);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @PatchMapping("/review")
    public ResponseEntity<Review> updateReview()
    {
        return new ResponseEntity<>(new Review(), HttpStatus.CREATED);
    }

    @DeleteMapping("/review")
    public ResponseEntity<Review> deleteReview()
    {
        return new ResponseEntity<>(new Review(), HttpStatus.ACCEPTED);
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviews()
    {
        List<Review> allReviews = reviewRepository.findAll();
        return new ResponseEntity<>(allReviews, HttpStatus.OK);
    }
}
