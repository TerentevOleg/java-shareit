package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(staticName = "defaultBuilder")
@AllArgsConstructor(staticName = "all")
@Setter
@Accessors(chain = true, fluent = true)
public class BookingBuilderTestUtils {
    private Long id;
    private LocalDateTime start = LocalDateTime.now().plusDays(2);
    private LocalDateTime end = LocalDateTime.now().plusDays(4);
    private Item item;
    private User booker;
    private BookingStatus status = BookingStatus.WAITING;

    public Booking bookingBuild() {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }
}
