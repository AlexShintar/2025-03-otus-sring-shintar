package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import ru.otus.hw.models.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @NonNull
    @EntityGraph(value = "comments-books-entity-graph")
    List<Comment> findAll();

    @NonNull
    @EntityGraph(value = "comments-books-entity-graph")
    List<Comment> findAllByBookId(long bookId);
}