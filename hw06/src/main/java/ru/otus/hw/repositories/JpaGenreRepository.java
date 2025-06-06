package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.Collections;
import java.util.List;
import java.util.Set;


@Repository
public class JpaGenreRepository implements GenreRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Genre> findAll() {
        String jpql = "SELECT g FROM Genre g";
        TypedQuery<Genre> query = em.createQuery(jpql, Genre.class);
        return query.getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String jpql = "SELECT g FROM Genre g WHERE g.id IN :ids";
        TypedQuery<Genre> query = em.createQuery(jpql, Genre.class);
        query.setParameter("ids", ids);
        return query.getResultList();
    }
}