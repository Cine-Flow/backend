package com.android.cineflow.service.user;

import com.android.cineflow.dto.request.UpdateWatchHistoryRequest;
import com.android.cineflow.dto.response.*;
import com.android.cineflow.exceptions.ResourceNotFoundException;
import com.android.cineflow.model.*;
import com.android.cineflow.model.enums.SubscriptionStatus;
import com.android.cineflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserContentService {
    private final CurrentUserService currentUserService;
    private final FavoriteRepository favoriteRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final FilmRepository filmRepository;
    private final EpisodeRepository episodeRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<FavoriteDto> getFavorites() {
        return favoriteRepository.findByUserIdOrderByAddedAtDesc(currentUserService.getCurrentUser().getId())
                .stream().map(this::toFavoriteDto).toList();
    }

    @Transactional
    public FavoriteDto addFavorite(Integer filmId) {
        User user = currentUserService.getCurrentUser();
        return favoriteRepository.findByUserIdAndFilmId(user.getId(), filmId)
                .map(this::toFavoriteDto)
                .orElseGet(() -> {
                    Film film = filmRepository.findById(filmId)
                            .orElseThrow(() -> new ResourceNotFoundException("Film not found with id: " + filmId));
                    Favorite favorite = Favorite.builder()
                            .user(user).film(film).addedAt(LocalDateTime.now()).build();
                    return toFavoriteDto(favoriteRepository.save(favorite));
                });
    }

    @Transactional
    public void deleteFavorite(Integer filmId) {
        User user = currentUserService.getCurrentUser();
        Favorite favorite = favoriteRepository.findByUserIdAndFilmId(user.getId(), filmId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));
        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public List<WatchHistoryDto> getWatchHistory() {
        return watchHistoryRepository.findByUserIdOrderByLastWatchedAtDesc(currentUserService.getCurrentUser().getId())
                .stream().map(this::toWatchHistoryDto).toList();
    }

    @Transactional
    public WatchHistoryDto updateWatchHistory(Integer episodeId, UpdateWatchHistoryRequest request) {
        User user = currentUserService.getCurrentUser();
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found with id: " + episodeId));
        WatchHistory history = watchHistoryRepository.findByUserIdAndEpisodeId(user.getId(), episodeId)
                .orElseGet(() -> WatchHistory.builder().user(user).episode(episode).build());
        history.setResumePositionSeconds(request.getResumePositionSeconds());
        history.setLastWatchedAt(LocalDateTime.now());
        return toWatchHistoryDto(watchHistoryRepository.save(history));
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile() {
        User user = currentUserService.getCurrentUser();
        return UserProfileDto.builder()
                .id(user.getId()).username(user.getUsername()).email(user.getEmail())
                .fullName(user.getFullName()).avatarUrl(user.getAvatarUrl()).role(user.getRole().name())
                .favoriteCount(favoriteRepository.countByUserId(user.getId()))
                .watchHistoryCount(watchHistoryRepository.countByUserId(user.getId()))
                .currentSubscription(getCurrentSubscription(user.getId()))
                .build();
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getCurrentSubscription() {
        return getCurrentSubscription(currentUserService.getCurrentUser().getId());
    }

    private SubscriptionDto getCurrentSubscription(String userId) {
        return subscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(userId, SubscriptionStatus.ACTIVE)
                .map(this::toSubscriptionDto).orElse(null);
    }

    private FavoriteDto toFavoriteDto(Favorite favorite) {
        return FavoriteDto.builder().id(favorite.getId())
                .film(modelMapper.map(favorite.getFilm(), FilmResponseDto.class))
                .addedAt(favorite.getAddedAt()).build();
    }

    private WatchHistoryDto toWatchHistoryDto(WatchHistory history) {
        return WatchHistoryDto.builder().id(history.getId())
                .film(modelMapper.map(history.getEpisode().getFilm(), FilmResponseDto.class))
                .episode(modelMapper.map(history.getEpisode(), EpisodeDto.class))
                .resumePositionSeconds(history.getResumePositionSeconds())
                .lastWatchedAt(history.getLastWatchedAt()).build();
    }

    private SubscriptionDto toSubscriptionDto(UserSubscription subscription) {
        return SubscriptionDto.builder().id(subscription.getId())
                .packageName(subscription.getSubscriptionPackage().getName())
                .price(subscription.getSubscriptionPackage().getPrice())
                .startDate(subscription.getStartDate()).endDate(subscription.getEndDate())
                .status(subscription.getStatus()).build();
    }
}
