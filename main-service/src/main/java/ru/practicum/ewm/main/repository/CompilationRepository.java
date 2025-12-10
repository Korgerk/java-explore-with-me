package ru.practicum.ewm.main.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.main.model.Compilation;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CompilationRepository {

    private final EntityManager em;

    public Compilation save(Compilation compilation) {
        if (compilation.getId() == null) {
            em.persist(compilation);
        } else {
            em.merge(compilation);
        }
        return compilation;
    }

    public Optional<Compilation> findById(Long id) {
        return Optional.ofNullable(em.find(Compilation.class, id));
    }

    public List<Compilation> findByPinned(Boolean pinned, int from, int size) {
        String jpql = pinned == null
                ? "SELECT c FROM Compilation c"
                : "SELECT c FROM Compilation c WHERE c.pinned = :pinned";
        TypedQuery<Compilation> query = em.createQuery(jpql, Compilation.class);
        if (pinned != null) {
            query.setParameter("pinned", pinned);
        }
        return query.setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Compilation> findAll(int from, int size) {
        return em.createQuery("SELECT c FROM Compilation c", Compilation.class)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(em::remove);
    }
}