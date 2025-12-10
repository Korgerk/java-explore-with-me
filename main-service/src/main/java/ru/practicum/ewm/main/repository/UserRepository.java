package ru.practicum.ewm.main.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.main.model.User;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public User save(User user) {
        if (user.getId() == null) {
            em.persist(user);
        } else {
            em.merge(user);
        }
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public List<User> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id IN :ids", User.class);
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    public List<User> findAll(int from, int size) {
        return em.createQuery("SELECT u FROM User u", User.class)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(em::remove);
    }
}