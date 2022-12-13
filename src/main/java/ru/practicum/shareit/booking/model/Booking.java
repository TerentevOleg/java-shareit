package ru.practicum.shareit.booking.model;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Item item;
    private User userBooked;
    private BookingStatus bookingStatus;
}
