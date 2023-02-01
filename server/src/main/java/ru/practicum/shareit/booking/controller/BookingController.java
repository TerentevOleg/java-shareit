package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingDtoResponse getBookingById(@PathVariable long bookingId,
                                             @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoResponse> getAllBookingsByBooker(
            @RequestParam String state,
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam Long from,
            @RequestParam Integer size) {
        return bookingService.getAllBookingsByBooker(userId, BookingState.converter(state), from, size);
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> getAllBookingsByOwner(
            @RequestParam String state,
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam Long from,
            @RequestParam Integer size) {
        return bookingService.getAllBookingsByOwner(userId, BookingState.converter(state), from, size);
    }

    @PostMapping
    public BookingDtoResponse add(@RequestBody BookingDtoRequest bookingDto,
                                  @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.add(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse bookingStatus(@PathVariable long bookingId,
                                            @RequestParam boolean approved,
                                            @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.bookingStatus(bookingId, userId, approved);
    }
}