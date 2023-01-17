package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.CustomValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDtoResponse getBookingById(long bookingId, long userId) {
        Booking booking = bookingRepository.findByIdAndItemOwnerIdOrBookerId(bookingId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "BookingServiceImpl: booking with id=" + bookingId + " and owner or booker id=" +
                                userId + " not found."));
        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDtoResponse> getAllBookingsByBooker(long bookerId, String state) {
        User booker = getUser(bookerId);
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case "PAST":
                bookings = bookingRepository.findByBookerAndEndIsBeforeOrderByStartDesc(booker, now);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerAndStartIsAfterOrderByStartDesc(booker, now);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                        booker, now, now);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerAndStatusIsOrderByStartDesc(booker, BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerAndStatusIsOrderByStartDesc(booker, BookingStatus.REJECTED);
                break;
            case "ALL":
                bookings = bookingRepository.findByBookerOrderByStartDesc(booker);
                break;
            default:
                throw new CustomValidationException("Unknown state: " + state);
        }
        return bookingMapper.toDto(bookings);
    }

    @Override
    public List<BookingDtoResponse> getAllBookingsByOwner(long ownerId, String state) {
        User owner = getUser(ownerId);
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case "PAST":
                bookings = bookingRepository.findByItemOwnerAndEndIsBeforeOrderByStartDesc(owner, now);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerAndStartIsAfterOrderByStartDesc(owner, now);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                        owner, now, now);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerAndStatusIsOrderByStartDesc(owner, BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerAndStatusIsOrderByStartDesc(owner, BookingStatus.REJECTED);
                break;
            case "ALL":
                bookings = bookingRepository.findByItemOwnerOrderByStartDesc(owner);
                break;
            default:
                throw new CustomValidationException("Unknown state: " + state);
        }
        return bookingMapper.toDto(bookings);
    }

    @Override
    @Transactional
    public BookingDtoResponse add(BookingDtoRequest bookingDtoRequest, long userId) {
        Item item = itemRepository.findByIdAndOwnerIdNot(bookingDtoRequest.getItemId(), userId)
                .orElseThrow(() -> new NotFoundException(
                        "BookingServiceImpl: item with id=" + bookingDtoRequest.getItemId() + " and owner id=" +
                                userId + " not found."));
        if (!item.getAvailable()) {
            throw new CustomValidationException("BookingServiceImpl: item id=" + item.getId() + " isn't available.");
        }
        Booking booking = bookingMapper.fromDto(bookingDtoRequest);
        checkDates(booking);
        User booker = getUser(userId);

        booking.setBooker(booker);
        booking.setItem(item);
        booking = bookingRepository.save(booking);
        log.debug("BookingServiceImpl: add booking " + booking + ".");
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDtoResponse bookingStatus(long bookingId, long userId, boolean approved) {
        Booking booking = bookingRepository.findByIdAndItemOwnerId(bookingId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "BookingServiceImpl: booking with id=" + bookingId + " and owner id=" +
                                userId + " not found."));
        if (!BookingStatus.WAITING.equals(booking.getStatus())) {
            throw new CustomValidationException("BookingServiceImpl: bookingServiceImpl: booking " +
                    "already has been approved/rejected");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.debug("BookingServiceImpl: approved booking " + booking + ".");
        return bookingMapper.toDto(booking);
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("BookingServiceImpl: user with id=" +
                        userId + " not found."));
    }

    private void checkDates(@NotNull Booking booking) {
        LocalDateTime start = Objects.requireNonNull(booking.getStart());
        LocalDateTime end = Objects.requireNonNull(booking.getEnd());
        if (end.isBefore(start) || end.equals(start) || start.isBefore(LocalDateTime.now())) {
            throw new CustomValidationException("BookingServiceImpl: incorrect booking date-time.");
        }
    }
}