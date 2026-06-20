package com.android.cineflow.service.admin;

import com.android.cineflow.dto.request.AdminCategoryRequest;
import com.android.cineflow.dto.request.AdminUserRequest;
import com.android.cineflow.dto.response.AdminAnalyticsDto;
import com.android.cineflow.dto.response.AdminCategoryDto;
import com.android.cineflow.dto.response.AdminUserDto;
import com.android.cineflow.exceptions.DuplicateResourceException;
import com.android.cineflow.exceptions.ResourceNotFoundException;
import com.android.cineflow.model.Category;
import com.android.cineflow.model.Episode;
import com.android.cineflow.model.Film;
import com.android.cineflow.model.User;
import com.android.cineflow.model.UserSubscription;
import com.android.cineflow.model.enums.SubscriptionStatus;
import com.android.cineflow.model.enums.UserRole;
import com.android.cineflow.repository.CategoryRepository;
import com.android.cineflow.repository.EpisodeRepository;
import com.android.cineflow.repository.FilmRepository;
import com.android.cineflow.repository.UserRepository;
import com.android.cineflow.repository.UserSubscriptionRepository;
import com.android.cineflow.repository.WatchHistoryRepository;
import com.android.cineflow.service.email.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final FilmRepository filmRepository;
    private final EpisodeRepository episodeRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;

    @Transactional(readOnly = true)
    public List<AdminCategoryDto> getCategories() {
        Map<Integer, Long> filmCounts = categoryRepository.countFilmsByCategory().stream()
                .collect(Collectors.toMap(row -> (Integer) row[0], row -> (Long) row[1]));
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getId))
                .map(c -> toCategoryDto(c, filmCounts.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    @Transactional
    public AdminCategoryDto createCategory(AdminCategoryRequest request) {
        Category saved = categoryRepository.save(Category.builder()
                .name(request.getName().trim())
                .description(blankToNull(request.getDescription()))
                .build());
        return toCategoryDto(saved, 0L);
    }

    @Transactional
    public AdminCategoryDto updateCategory(Integer id, AdminCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setName(request.getName().trim());
        category.setDescription(blankToNull(request.getDescription()));
        Category saved = categoryRepository.save(category);
        long count = categoryRepository.countFilmsByCategory().stream()
                .filter(row -> id.equals(row[0]))
                .map(row -> (Long) row[1])
                .findFirst()
                .orElse(0L);
        return toCategoryDto(saved, count);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AdminUserDto> getUsers(String search, String role, String subscription) {
        String q = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String roleFilter = role == null ? "" : role.trim();
        String subFilter = subscription == null ? "" : subscription.trim();

        Map<String, UserSubscription> activeSubs = subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .collect(Collectors.toMap(s -> s.getUser().getId(), Function.identity(),
                        (a, b) -> a.getEndDate().isAfter(b.getEndDate()) ? a : b));

        return userRepository.findAll().stream()
                .filter(u -> q.isEmpty()
                        || contains(u.getUsername(), q)
                        || contains(u.getEmail(), q)
                        || contains(u.getFullName(), q)
                        || contains(u.getPhoneNumber(), q))
                .filter(u -> roleFilter.isEmpty() || "ALL".equalsIgnoreCase(roleFilter)
                        || u.getRole().name().equalsIgnoreCase(roleFilter))
                .filter(u -> {
                    boolean premium = activeSubs.containsKey(u.getId());
                    return subFilter.isEmpty() || "ALL".equalsIgnoreCase(subFilter)
                            || ("PREMIUM".equalsIgnoreCase(subFilter) && premium)
                            || ("FREE".equalsIgnoreCase(subFilter) && !premium);
                })
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(u -> toUserDto(u, activeSubs.get(u.getId())))
                .toList();
    }

    @Transactional
    public AdminUserDto createUser(AdminUserRequest request) {
        validateUniqueUser(request, null);
        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(blankToNull(request.getFullName()))
                .phoneNumber(blankToNull(request.getPhoneNumber()))
                .avatarUrl(blankToNull(request.getAvatarUrl()))
                .role(request.getRole() != null ? request.getRole() : UserRole.ROLE_USER)
                .build();
        return toUserDto(userRepository.save(user), null);
    }

    @Transactional
    public AdminUserDto updateUser(String id, AdminUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        validateUniqueUser(request, id);
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setFullName(blankToNull(request.getFullName()));
        user.setPhoneNumber(blankToNull(request.getPhoneNumber()));
        user.setAvatarUrl(blankToNull(request.getAvatarUrl()));
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toUserDto(userRepository.save(user), getActiveSubscription(id));
    }

    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void resetUserPassword(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(Instant.now().plus(RESET_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        userRepository.save(user);
        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsDto getAnalytics(int period) {
        int days = period == 7 || period == 90 ? period : 30;
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        LocalDateTime sinceLocal = LocalDateTime.now().minusDays(days);

        List<User> users = userRepository.findAll();
        List<Film> films = filmRepository.findAll();
        List<Episode> episodes = episodeRepository.findAll();
        List<UserSubscription> subscriptions = subscriptionRepository.findAll();
        List<UserSubscription> activeSubscriptions = subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();

        long premiumUsers = activeSubscriptions.stream()
                .map(s -> s.getUser().getId())
                .distinct()
                .count();
        BigDecimal revenue = activeSubscriptions.stream()
                .map(s -> s.getSubscriptionPackage().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminAnalyticsDto.builder()
                .period(days)
                .totalUsers(users.size())
                .newSignups(users.stream().filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(since)).count())
                .episodeViews(episodes.stream().mapToLong(e -> e.getViewCount() != null ? e.getViewCount() : 0).sum())
                .watchSessions(watchHistoryRepository.countByLastWatchedAtAfter(sinceLocal))
                .premiumUsers(premiumUsers)
                .revenue(revenue)
                .filmTypes(films.stream()
                        .collect(Collectors.groupingBy(f -> f.getType().name(), Collectors.counting()))
                        .entrySet().stream()
                        .map(e -> new AdminAnalyticsDto.MetricSlice(e.getKey(), e.getValue()))
                        .toList())
                .subscriptions(activeSubscriptions.stream()
                        .collect(Collectors.groupingBy(s -> s.getSubscriptionPackage().getName(), Collectors.counting()))
                        .entrySet().stream()
                        .map(e -> new AdminAnalyticsDto.MetricSlice(e.getKey(), e.getValue()))
                        .toList())
                .topCategories(getCategories().stream()
                        .sorted(Comparator.comparing(AdminCategoryDto::getFilmCount).reversed())
                        .limit(6)
                        .map(c -> new AdminAnalyticsDto.MetricBar(c.getName(), c.getFilmCount()))
                        .toList())
                .topFilms(topFilms(films, episodes))
                .build();
    }

    private List<AdminAnalyticsDto.MetricBar> topFilms(List<Film> films, List<Episode> episodes) {
        Map<Integer, Long> viewsByFilm = episodes.stream()
                .collect(Collectors.groupingBy(e -> e.getFilm().getId(),
                        Collectors.summingLong(e -> e.getViewCount() != null ? e.getViewCount() : 0)));
        List<AdminAnalyticsDto.MetricBar> bars = new ArrayList<>();
        films.stream()
                .sorted(Comparator.comparing((Film f) -> viewsByFilm.getOrDefault(f.getId(), 0L)).reversed())
                .limit(6)
                .forEach(f -> bars.add(new AdminAnalyticsDto.MetricBar(f.getTitle(), viewsByFilm.getOrDefault(f.getId(), 0L))));
        return bars;
    }

    private AdminCategoryDto toCategoryDto(Category category, long filmCount) {
        return AdminCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .filmCount(filmCount)
                .build();
    }

    private AdminUserDto toUserDto(User user, UserSubscription subscription) {
        return AdminUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .subscriptionPlan(subscription != null ? subscription.getSubscriptionPackage().getName() : null)
                .subscriptionEndDate(subscription != null ? subscription.getEndDate() : null)
                .build();
    }

    private UserSubscription getActiveSubscription(String userId) {
        return subscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(userId, SubscriptionStatus.ACTIVE)
                .orElse(null);
    }

    private void validateUniqueUser(AdminUserRequest request, String currentId) {
        userRepository.findByUsername(request.getUsername().trim())
                .filter(u -> !u.getId().equals(currentId))
                .ifPresent(u -> { throw new DuplicateResourceException("Username already exists"); });
        userRepository.findByEmail(request.getEmail().trim())
                .filter(u -> !u.getId().equals(currentId))
                .ifPresent(u -> { throw new DuplicateResourceException("Email already exists"); });
        String phone = blankToNull(request.getPhoneNumber());
        if (phone != null) {
            userRepository.findByPhoneNumber(phone)
                    .filter(u -> !u.getId().equals(currentId))
                    .ifPresent(u -> { throw new DuplicateResourceException("Phone number already exists"); });
        }
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
