package com.mtech.webapp.controllers;

import com.mtech.webapp.models.*;
import com.mtech.webapp.repositories.ReviewRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
public class ReviewController {
    @Autowired
    private ReviewRepository reviewRepository;

    @PostMapping("/review")
    @Tag(name = "Platform Reviews")
    @Operation(summary = "Add a review", description = "Add platform review",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Add Review Payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"email\": \"abc@gmail.com\", \"rating\": 5, \"description\": \"This platform helped me\", \"anonymity\": true }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Review updated successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<Review> postReview(@RequestBody ReviewRequest reviewRequest)
    {
        System.out.println("Add review given by : "+ reviewRequest.getEmail());
        Review review = new Review();
        review.setAnonymity(reviewRequest.isAnonymity());
        review.setDescription(reviewRequest.getDescription());
        review.setRating(reviewRequest.getRating());
        review.setEmail(reviewRequest.getEmail());
        reviewRepository.save(review);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @PatchMapping("/review")
    @Tag(name = "Platform Reviews")
    @Operation(summary = "Patch a review", description = "Update an existing review",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Update Review Payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewUpdateRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"reviewId\": \"a102gth356\", \"rating\": 5, \"description\": \"This platform helped me\", \"anonymity\": true }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Review updated successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
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
    @Tag(name = "Platform Reviews")
    @Operation(summary = "Delete a review", description = "Delete an existing review",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Delete Review Payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewDeleteRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"reviewId\": \"a102gth356\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Review deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<Review> deleteReview(@RequestBody ReviewDeleteRequest reviewDeleteRequest)
    {
        int noOfEntriesDeleted = reviewRepository.deleteByReviewId(reviewDeleteRequest.getReviewId());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/reviews")
    @Tag(name = "Platform Reviews")
    @Operation(summary = "Get all platform reviews", description = "Retrieve all platform reviews provided by users",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved all platform reviews provided by users"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<List<Review>> getReviews()
    {
        System.out.println("Getting all reviews : ");
        List<Review> allReviews = reviewRepository.findAll();
        Collections.reverse(allReviews);
        return new ResponseEntity<>(allReviews, HttpStatus.OK);
    }

    @GetMapping("/reviews/rating/{ratingValue}")
    @Tag(name = "Platform Reviews")
    @Operation(summary = "Get all platform reviews of certain rating", description = "Retrieve all platform reviews provided by users which are of given rating",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved all platform reviews"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Error occurred while Processing Request at the Server Side")
            })
    public ResponseEntity<List<Review>> filterReviewsByRating(@PathVariable @Parameter(example = "1") int ratingValue)
    {
        List<Review> allReviews = reviewRepository.findByRating(ratingValue);
        return new ResponseEntity<>(allReviews, HttpStatus.OK);
    }
}
