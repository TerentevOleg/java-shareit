package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS item " +
            "JOIN item.owner AS owner " +
            "JOIN b.booker AS booker " +
            "WHERE b.id = :bookingId AND (owner.id = :userId OR booker.id = :userId)")
    Optional<Booking> findByIdAndItemOwnerIdOrBookerId(@Param("bookingId") Long id, @Param("userId") Long userId);

    Optional<Booking> findByIdAndItemOwnerId(Long id, Long ownerId);

    Optional<Booking> findFirstByItemAndStartAfterAndStatusIsOrderByStartDesc(
            Item item, LocalDateTime minStart, BookingStatus status);

    Optional<Booking> findFirstByItemAndStartLessThanEqualAndStatusIsOrderByStartDesc(
            Item item, LocalDateTime maxStart, BookingStatus status);

    Optional<Booking> findFirstByItemAndBookerAndEndBefore(Item item, User booker, LocalDateTime maxEnd);

    List<Booking> findByBookerOrderByStartDesc(User booker);

    List<Booking> findByItemOwnerOrderByStartDesc(User booker);

    List<Booking> findByBookerAndStartIsAfterOrderByStartDesc(User booker, LocalDateTime minStart);

    List<Booking> findByBookerAndEndIsBeforeOrderByStartDesc(User booker, LocalDateTime maxEnd);

    List<Booking> findByBookerAndStatusIsOrderByStartDesc(User booker, BookingStatus status);

    List<Booking> findByItemOwnerAndStartIsAfterOrderByStartDesc(User booker, LocalDateTime minStart);

    List<Booking> findByItemOwnerAndEndIsBeforeOrderByStartDesc(User booker, LocalDateTime maxEnd);

    List<Booking> findByItemOwnerAndStatusIsOrderByStartDesc(User booker, BookingStatus status);

    List<Booking> findByBookerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
            User booker, LocalDateTime maxStart, LocalDateTime minEnd);

    List<Booking> findByItemOwnerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
            User booker, LocalDateTime maxStart, LocalDateTime minEnd);

    List<Booking> findAllByItemInAndStartAfterAndStatusIsOrderByStartDesc(
            List<Item> items, LocalDateTime minStart, BookingStatus status);

    List<Booking> findAllByItemInAndStartLessThanEqualAndStatusIsOrderByStartDesc(
            List<Item> items, LocalDateTime maxStart, BookingStatus status);
}