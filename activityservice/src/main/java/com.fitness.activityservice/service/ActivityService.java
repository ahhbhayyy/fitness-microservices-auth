package com.fitness.activityservice.service;

import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
@Slf4j
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final KafkaTemplate<String,Activity> kafkaTemplate;
    @Value("${kafka.topic.name}")
    private String topicName;
    public ActivityResponse trackActivity(ActivityRequest request) {

        boolean isValidUser=userValidationService.validateUser(request.getUserId());
        if(!isValidUser){
            throw new RuntimeException("invalid user: "+request.getUserId());
        }

        Activity activity=Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();

        Activity savedActivity=activityRepository.save(activity);
        kafkaTemplate.send(topicName,savedActivity.getUserId(),savedActivity)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish activity event: activityId={}, userId={}", savedActivity.getId(), savedActivity.getUserId(), ex);
                        return;
                    }
                    log.info("Published activity event: topic={}, partition={}, offset={}, activityId={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            savedActivity.getId());
                });
        return mapToResponse(savedActivity);
    }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response=new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }
}
