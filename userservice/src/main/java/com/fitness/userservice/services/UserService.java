package com.fitness.userservice.services;

import com.fitness.userservice.UserRepository;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    public UserResponse register(RegisterRequest request) {
        if (request.getKeycloakId() != null && userRepository.existsByKeycloakId(request.getKeycloakId())) {
            return mapToResponse(userRepository.findByKeycloakId(request.getKeycloakId()));
        }

        if(userRepository.existsByEmail(request.getEmail())){
            User existingUser= userRepository.findByEmail(request.getEmail());
            if (existingUser.getKeycloakId() == null && request.getKeycloakId() != null) {
                existingUser.setKeycloakId(request.getKeycloakId());
                existingUser = userRepository.save(existingUser);
            }
            return mapToResponse(existingUser);
        }
        User user = new User();
        user.setKeycloakId(request.getKeycloakId());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User savedUser= userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findByKeycloakId(userId);
        if (user == null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("user not found"));
        }
        return mapToResponse(user);
    }

    public Boolean existByUserId(String userId) {
        log.info("Calling user service for {}",userId);
        return userRepository.existsByKeycloakId(userId);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse userResponse=new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setKeycloakId(user.getKeycloakId());
        userResponse.setEmail(user.getEmail());
        userResponse.setPassword(user.getPassword());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setCreateAt(user.getCreateAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }
}
