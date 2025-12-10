package ru.practicum.ewm.main.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.main.model.Category;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {

    private final EntityManager em;

    public Category save(Category category) {
        if (category.getId() == null) {
            em.persist(category);
        } else {
            em.merge(category);
        }
        return category;
    }

    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(em.find(Category.class, id));
    }

    public List<Category> findAll(int from, int size) {
        return em.createQuery("SELECT c FROM Category c", Category.class)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(em::remove);
    }
}