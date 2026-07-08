package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    private final ActivityService activityService;

    @PostMapping("")
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request){
        try {
            return ResponseEntity.ok(activityService.trackActivity(request));
        } catch (Exception e) {
            e.printStackTrace(); // ADD THIS
            return ResponseEntity.badRequest().body(null);
        }
    }
}
