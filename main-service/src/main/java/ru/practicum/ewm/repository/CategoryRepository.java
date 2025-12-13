package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    Page<Category> findAll(Pageable pageable);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.category.id = :categoryId")
    boolean hasEvents(@Param("categoryId") Long categoryId);
}