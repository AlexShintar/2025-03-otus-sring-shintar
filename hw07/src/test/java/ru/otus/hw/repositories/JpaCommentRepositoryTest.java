package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.Arguments;

@DisplayName("Репозиторий на основе JPA для работы с комментариями")
@DataJpaTest
class JpaCommentRepositoryTest {

    @Autowired
    private CommentRepository repository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен загружать комментарий по id")
    @ParameterizedTest
    @MethodSource("getDbComments")
    void shouldReturnCorrectCommentById(Comment template) {
        Comment expectedComment = em.find(Comment.class, template.getId());
        Optional<Comment> actualComment = repository.findById(template.getId());

        assertThat(actualComment).isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен возвращать пустой Optional для несуществующего id")
    @Test
    void shouldReturnEmptyOptionalForNonExistentId() {
        Optional<Comment> actualComment = repository.findById(999L);
        assertThat(actualComment).isEmpty();
    }

    @DisplayName("должен загружать все комментарии по id книги")
    @ParameterizedTest(name = "для книги с id={0}")
    @MethodSource("getDbCommentsByBookId")
    void shouldReturnCorrectCommentsByBookId(long bookId, List<Comment> expectedComments) {
        List<Comment> expectedCommentsFromDb = expectedComments.stream()
                .map(c -> em.find(Comment.class, c.getId()))
                .toList();

        List<Comment> actualComments = repository.findAllByBookId(bookId);

        assertThat(actualComments)
                .containsExactlyElementsOf(expectedCommentsFromDb);
    }

    @DisplayName("должен возвращать пустой список для книги без комментариев")
    @Test
    void shouldReturnEmptyListForBookWithoutComments() {
        List<Comment> actualComments = repository.findAllByBookId(999L);
        assertThat(actualComments).isEmpty();
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        Book book = em.find(Book.class, 2L);
        Comment newComment = new Comment(0, "Test Comment", book);

        Comment savedComment = repository.save(newComment);

        assertThat(savedComment)
                .isNotNull()
                .matches(comment -> comment.getId() > 0)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(newComment);

        Comment foundComment = em.find(Comment.class, savedComment.getId());
        assertThat(foundComment)
                .usingRecursiveComparison()
                .isEqualTo(savedComment);
    }

    @DisplayName("должен обновлять существующий комментарий")
    @Test
    void shouldUpdateComment() {
        Comment existingComment = em.find(Comment.class, 2L);
        String originalContent = existingComment.getContent();
        existingComment.setContent("Updated content");

        Comment updatedComment = repository.save(existingComment);
        em.flush();

        assertThat(updatedComment)
                .isNotNull()
                .matches(comment -> comment.getId() == 2L)
                .usingRecursiveComparison()
                .isEqualTo(existingComment);

        Comment foundComment = em.find(Comment.class, updatedComment.getId());
        assertThat(foundComment.getContent())
                .isEqualTo("Updated content")
                .isNotEqualTo(originalContent);
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteCommentById() {
        long commentId = 3L;
        assertThat(em.find(Comment.class, commentId)).isNotNull();

        repository.deleteById(commentId);
        em.flush();

        assertThat(em.find(Comment.class, commentId)).isNull();
    }


    private static Stream<Comment> getDbComments() {
        return IntStream.rangeClosed(1, 6)
                .mapToObj(id -> {
                    Comment comment = new Comment();
                    comment.setId(id);
                    return comment;
                });
    }

    private static Stream<Arguments> getDbCommentsByBookId() {
        var data = Map.of(1L, List.of(1L, 2L, 3L), 2L, List.of(4L, 5L), 3L, List.of(6L));
        return data.entrySet().stream()
                .map(e -> arguments(e.getKey(),
                        e.getValue().stream()
                                .map(id -> { Comment c = new Comment(); c.setId(id); return c; })
                                .toList()));
    }
}