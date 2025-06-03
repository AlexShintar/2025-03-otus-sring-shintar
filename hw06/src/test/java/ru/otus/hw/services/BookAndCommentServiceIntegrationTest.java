package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Интеграционные тесты")
class BookAndCommentServiceIntegrationTest  {

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentService commentService;

    @Test
    @DisplayName("Должны возвращаться BookDto с заполненными связями")
    void shouldReturnBookDtosWithRelations() {
        List<BookDto> books = bookService.findAll();
        assertThat(books).isNotEmpty();

        BookDto book = books.get(0);

        assertThat(book.getAuthor()).isNotNull();
        assertThat(book.getAuthor().getFullName()).isNotEmpty();

        assertThat(book.getGenres()).isNotNull();
        assertThat(book.getGenres()).isNotEmpty();
        assertThat(book.getGenres().get(0).getName()).isNotEmpty();
    }

    @Test
    @DisplayName("bookId в CommentDto должен ссылаться на существующую книгу")
    void commentBookIdShouldPointToExistingBook() {
        List<BookDto> books = bookService.findAll();
        assertThat(books).isNotEmpty();
        List<Long> allBookIds = books.stream()
                .map(BookDto::getId)
                .toList();

        List<CommentDto> allComments = allBookIds.stream()
                .flatMap(bookId -> commentService.findAllByBookId(bookId).stream())
                .toList();

        assertThat(allComments).isNotEmpty();

        for (CommentDto comment : allComments) {
            assertThat(comment.getBookId()).isIn(allBookIds);
        }
    }

    @Test
    @DisplayName("Удаление книги должно приводить к удалению связанных с ней комментариев")
    void shouldDeleteBookAndCascadeDeleteComments() {
        List<BookDto> books = bookService.findAll();
        assertThat(books).isNotEmpty();

        BookDto book = books.get(0);
        long bookId = book.getId();

        List<CommentDto> comments = commentService.findAllByBookId(bookId);
        assertThat(comments).isNotEmpty();

        bookService.deleteById(bookId);
        Optional<BookDto> deleted = bookService.findById(bookId);
        assertThat(deleted).isEmpty();

        List<CommentDto> deletedComments = commentService.findAllByBookId(bookId);
        assertThat(deletedComments).isEmpty();
    }
}

