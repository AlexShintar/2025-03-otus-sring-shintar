package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с жанрами")
@DataJpaTest
@Import(JpaGenreRepository.class)
class JpaGenreRepositoryTest {

    @Autowired
    private JpaGenreRepository repository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен загружать список жанров по списку id")
    @ParameterizedTest
    @MethodSource("getDbGenresByIds")
    void shouldReturnCorrectGenresByIds(Set<Long> ids) {
        List<Genre> expectedGenres = ids.stream()
                .map(id -> em.find(Genre.class, id))
                .toList();
        var actualGenres = repository.findAllByIds(ids);
        assertThat(actualGenres).containsExactlyInAnyOrderElementsOf(expectedGenres);
    }

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var actualGenres = repository.findAll();
        List<Genre> expectedGenres = getDbGenres().stream()
                .map(g -> em.find(Genre.class, g.getId()))
                .toList();

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
    }

    private static Stream<Arguments> getDbGenresByIds() {
        return Stream.of(
                Arguments.of(Set.of(1L)),
                Arguments.of(Set.of(2L, 5L)),
                Arguments.of(Set.of(3L, 4L, 6L))
        );
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }
}
