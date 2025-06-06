package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(value = "comments-books-entity-graph")
    List<Comment> findAll();

    @EntityGraph(value = "comments-books-entity-graph")
    List<Comment> findAllByBookId(long bookId);
}