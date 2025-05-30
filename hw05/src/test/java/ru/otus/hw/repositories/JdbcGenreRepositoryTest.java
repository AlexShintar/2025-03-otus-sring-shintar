package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jdbc для работы с жанрами")
@JdbcTest
@Import(JdbcGenreRepository.class)
class JdbcGenreRepositoryTest {

    @Autowired
    private JdbcGenreRepository repositoryJdbc;

    @DisplayName("должен загружать список жанров по списку id")
    @ParameterizedTest
    @MethodSource("getDbGenresByIds")
    void shouldReturnCorrectGenresByIds(Set<Long> ids, List<Genre> expectedGenres) {
        var actualGenres = repositoryJdbc.findAllByIds(ids);
        assertThat(actualGenres).containsExactlyInAnyOrderElementsOf(expectedGenres);
    }

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var actualGenres = repositoryJdbc.findAll();
        var expectedGenres = getDbGenres();
        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
    }

    static Stream<Arguments> getDbGenresByIds() {
        var allGenres = getDbGenres();
        return Stream.of(
                        Set.of(1L),
                        Set.of(2L, 5L),
                        Set.of(3L, 4L, 6L)
                )
                .map(ids -> Arguments.of(
                        ids,
                        allGenres.stream()
                                .filter(g -> ids.contains(g.getId()))
                                .toList()
                ));
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }
}
