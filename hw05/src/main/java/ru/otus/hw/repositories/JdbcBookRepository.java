package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<Book> findById(long id) {
        String sql = """
                SELECT b.id,
                       b.title,
                       b.author_id,
                       a.full_name AS author_name,
                       g.id        AS genre_id,
                       g.name      AS genre_name
                FROM books b
                  JOIN authors a ON b.author_id = a.id
                  LEFT JOIN books_genres bg ON b.id = bg.book_id
                  LEFT JOIN genres g        ON bg.genre_id = g.id
                WHERE b.id = :id
                """;
        var params = Map.of("id", id);
        List<Book> books = jdbc.query(sql, params, new BookResultSetExtractor());

        return books.isEmpty() ? Optional.empty() : Optional.of(books.get(0));
    }

    @Override
    public List<Book> findAll() {
        String sql = """
                SELECT b.id, b.title, b.author_id, a.full_name AS author_name,
                       g.id AS genre_id, g.name AS genre_name
                FROM books b
                JOIN authors a ON b.author_id = a.id
                LEFT JOIN books_genres bg ON b.id = bg.book_id
                LEFT JOIN genres g ON bg.genre_id = g.id
                ORDER BY b.id
                """;

        return jdbc.query(sql, new BookResultSetExtractor());
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        String deleteBooksGenresQuery = "DELETE FROM books_genres WHERE book_id = :book_id";
        String deleteBooksQuery = "DELETE FROM books WHERE id = :id";

        var genresParams = Map.of("book_id", id);
        var booksParams = Map.of("id", id);

        jdbc.update(deleteBooksGenresQuery, genresParams);
        jdbc.update(deleteBooksQuery, booksParams);
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        var params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId());
        String sql = "INSERT INTO books (title, author_id) VALUES (:title, :author_id)";
        jdbc.update(sql, params, keyHolder, new String[]{"id"});
        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        var params = new MapSqlParameterSource()
                .addValue("id", book.getId())
                .addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId());
        String sql = "UPDATE books SET title = :title, author_id = :author_id WHERE id = :id";
        int updated = jdbc.update(sql, params);
        if (updated == 0) {
            throw new EntityNotFoundException("Book with id " + book.getId() + " not found");
        }
        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        if (book.getGenres().isEmpty()) {
            return;
        }
        var params = book.getGenres().stream()
                .map(genre -> new MapSqlParameterSource()
                        .addValue("book_id", book.getId())
                        .addValue("genre_id", genre.getId()))
                .toArray(MapSqlParameterSource[]::new);
        String sql = "INSERT INTO books_genres (book_id, genre_id) VALUES (:book_id, :genre_id)";
        jdbc.batchUpdate(sql, params);
    }

    private void removeGenresRelationsFor(Book book) {
        String sql = "DELETE FROM books_genres WHERE book_id = :book_id";
        var params = Map.of("book_id", book.getId());
        jdbc.update(sql, params);
    }

    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<List<Book>> {

        @Override
        public List<Book> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Book> booksMap = new LinkedHashMap<>();

            while (rs.next()) {
                long bookId = rs.getLong("id");
                Book book = booksMap.get(bookId);
                if (book == null) {
                    String title = rs.getString("title");
                    long authorId = rs.getLong("author_id");
                    String authorName = rs.getString("author_name");
                    Author author = new Author(authorId, authorName);
                    book = new Book(bookId, title, author, new ArrayList<>());
                    booksMap.put(bookId, book);
                }

                long genreId = rs.getLong("genre_id");
                if (!rs.wasNull()) {
                    String genreName = rs.getString("genre_name");
                    Genre genre = new Genre(genreId, genreName);
                    book.getGenres().add(genre);
                }
            }

            return new ArrayList<>(booksMap.values());
        }
    }
}
