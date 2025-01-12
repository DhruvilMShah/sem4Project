package com.mtech.webapp.controllers;

import com.mtech.webapp.models.*;
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
    public ResponseEntity<Review> postReview(@RequestBody ReviewRequest reviewRequest)
    {
        Review review = new Review();
        review.setAnonymity(reviewRequest.isAnonymity());
        review.setDescription(reviewRequest.getDescription());
        review.setRating(reviewRequest.getRating());
        review.setEmail(reviewRequest.getEmail());
        reviewRepository.save(review);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @PatchMapping("/review")
    public ResponseEntity<Review> updateReview(@RequestBody ReviewUpdateRequest reviewRequest)
    {
        Review review = reviewRepository.findByReviewId(reviewRequest.getReviewId());
        review.setAnonymity(reviewRequest.isAnonymity());
        review.setDescription(reviewRequest.getDescription());
        review.setRating(reviewRequest.getRating());
        reviewRepository.save(review);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @DeleteMapping("/review")
    public ResponseEntity<Review> deleteReview(@RequestBody ReviewDeleteRequest reviewDeleteRequest)
    {
        int noOfEntriesDeleted = reviewRepository.deleteByReviewId(reviewDeleteRequest.getReviewId());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviews()
    {
        List<Review> allReviews = reviewRepository.findAll();
        return new ResponseEntity<>(allReviews, HttpStatus.OK);
    }
}
