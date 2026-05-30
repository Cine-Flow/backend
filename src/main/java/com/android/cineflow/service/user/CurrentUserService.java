package com.android.cineflow.service.user;

import com.android.cineflow.exceptions.ResourceNotFoundException;
import com.android.cineflow.model.User;
import com.android.cineflow.repository.UserRepository;
import com.android.cineflow.security.userdetails.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AppUserDetails userDetails)) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
