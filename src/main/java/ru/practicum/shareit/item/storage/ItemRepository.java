package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByIdAndOwnerIdNot(Long id, Long ownerId);

    @Query(value = "SELECT i " +
            "FROM Item AS i " +
            "WHERE (lower(i.name) LIKE %:text% OR lower(i.description) LIKE %:text%) AND i.available=TRUE")
    List<Item> searchByNameOrDescription(@Param("text") String lowerText);

    List<Item> findAllByOwnerOrderByIdAsc(User owner);
}