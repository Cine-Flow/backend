package com.android.cineflow.service.film;

import com.android.cineflow.dto.request.CreateCommentRequest;
import com.android.cineflow.dto.response.CommentDto;
import com.android.cineflow.exceptions.ResourceNotFoundException;
import com.android.cineflow.model.Film;
import com.android.cineflow.model.FilmComment;
import com.android.cineflow.model.User;
import com.android.cineflow.repository.FilmCommentRepository;
import com.android.cineflow.repository.FilmRepository;
import com.android.cineflow.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmCommentService {

    private final FilmCommentRepository commentRepository;
    private final FilmRepository filmRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Integer filmId) {
        if (!filmRepository.existsById(filmId)) {
            throw new ResourceNotFoundException("Film not found with id: " + filmId);
        }
        return commentRepository.findByFilmIdOrderByCreatedAtDesc(filmId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public CommentDto createComment(Integer filmId, CreateCommentRequest request) {
        User user = currentUserService.getCurrentUser();
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new ResourceNotFoundException("Film not found with id: " + filmId));

        FilmComment comment = FilmComment.builder()
                .film(film)
                .user(user)
                .content(request.getContent().trim())
                .build();
        return toDto(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Integer commentId) {
        User user = currentUserService.getCurrentUser();
        FilmComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        boolean isOwner = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() != null
                && "ROLE_ADMIN".equals(user.getRole().name());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền xóa bình luận này");
        }
        commentRepository.delete(comment);
    }

    private CommentDto toDto(FilmComment c) {
        User u = c.getUser();
        return CommentDto.builder()
                .id(c.getId())
                .filmId(c.getFilm().getId())
                .userId(u.getId())
                .username(u.getUsername())
                .avatarUrl(u.getAvatarUrl())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
