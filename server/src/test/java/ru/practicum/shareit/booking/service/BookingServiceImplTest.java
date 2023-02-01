package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.mapper.BookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.CustomValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Spy
    private static BookingMapper bookingMapper = new BookingMapperImpl();

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Accessors(chain = true, fluent = true)
    private static class TestBookingBuilder {
        private Long id = 1L;
        private LocalDateTime start = LocalDateTime.now().minusDays(2);
        private LocalDateTime end = LocalDateTime.now().minusDays(1);
        private BookingStatus status = BookingStatus.WAITING;
        private Long itemId = 10L;
        private Boolean itemAvailable = true;
        private Long itemOwnerId = 20L;
        private Long bookerId = 30L;

        public Booking buildBooking() {
            Booking booking = new Booking();
            booking.setId(id);
            booking.setStart(start);
            booking.setEnd(end);
            booking.setStatus(status);
            Item item = new Item();
            item.setId(itemId);
            item.setAvailable(itemAvailable);
            User itemOwner = new User();
            itemOwner.setId(itemOwnerId);
            item.setOwner(itemOwner);
            booking.setItem(item);
            User booker = new User();
            booker.setId(bookerId);
            booking.setBooker(booker);
            return booking;
        }

        public BookingDtoRequest buildDtoRequest() {
            return new BookingDtoRequest(start, end, itemId);
        }

        public BookingDtoResponse buildDtoResponse() {
            return new BookingDtoResponse(id, start, end,
                    new ItemDto(itemId, null, null, itemAvailable, null),
                    new UserDto(bookerId, null, null),
                    status);
        }
    }

    @BeforeAll
    static void beforeAll() {
        ReflectionTestUtils.setField(bookingMapper, "userMapper", new UserMapperImpl());
        ReflectionTestUtils.setField(bookingMapper, "itemMapper", new ItemMapperImpl());
    }

    @Test
    void givenAddWithNoItemWithGivenIdAndOtherOwner_whenAdd_thenThrowNotFoundException() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder().id(null);
        Booking requestBooking = bookingBuilder.buildBooking();
        BookingDtoRequest requestDto = bookingBuilder.buildDtoRequest();
        long userId = requestBooking.getBooker().getId();

        Mockito.lenient().when(itemRepository.findByIdAndOwnerIdNot(requestDto.getItemId(), userId))
                .thenReturn(Optional.empty());
        Mockito.lenient().when(userRepository.findById(userId))
                .thenReturn(Optional.of(requestBooking.getBooker()));

        assertThrows(NotFoundException.class,
                () -> bookingService.add(requestDto, userId));

        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenAddWithUnavailableItem_whenAdd_thenThrowCustomValidationException() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder().itemAvailable(false).id(null);
        Booking requestBooking = bookingBuilder.buildBooking();
        BookingDtoRequest requestDto = bookingBuilder.buildDtoRequest();
        long userId = requestBooking.getBooker().getId();

        Mockito.lenient().when(itemRepository.findByIdAndOwnerIdNot(requestDto.getItemId(), userId))
                .thenReturn(Optional.of(requestBooking.getItem()));
        Mockito.lenient().when(userRepository.findById(userId))
                .thenReturn(Optional.of(requestBooking.getBooker()));

        assertThrows(CustomValidationException.class,
                () -> bookingService.add(requestDto, userId));

        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenAddWithIncorrectUserId_whenAdd_thenThrowNotFoundException() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder().id(null);
        Booking requestBooking = bookingBuilder.buildBooking();
        BookingDtoRequest requestDto = bookingBuilder.buildDtoRequest();
        long userId = requestBooking.getBooker().getId();

        Mockito.lenient().when(itemRepository.findByIdAndOwnerIdNot(requestDto.getItemId(), userId))
                .thenReturn(Optional.of(requestBooking.getItem()));
        Mockito.lenient().when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(CustomValidationException.class,
                () -> bookingService.add(requestDto, userId));

        Mockito.verify(bookingRepository, Mockito.never())
                .save(Mockito.any());
    }

    @Test
    void givenApproveCorrectWaitingBookingSetApproved_whenApprove_thenReturnDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder().status(BookingStatus.WAITING);
        Booking booking = bookingBuilder.buildBooking();
        bookingBuilder.status(BookingStatus.APPROVED);
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        long bookingId = booking.getId();
        long userId = booking.getItem().getOwner().getId();
        boolean approved = true;

        Mockito.when(bookingRepository.findByIdAndItemOwnerId(bookingId, userId))
                .thenReturn(Optional.of(booking));

        assertEquals(responseDto, bookingService.bookingStatus(bookingId, userId, approved));
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void givenApproveCorrectWaitingBookingSetRejected_whenApprove_thenReturnDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder().status(BookingStatus.WAITING);
        Booking booking = bookingBuilder.buildBooking();
        bookingBuilder.status(BookingStatus.REJECTED);
        BookingDtoResponse expectedDto = bookingBuilder.buildDtoResponse();
        long bookingId = booking.getId();
        long userId = booking.getItem().getOwner().getId();
        boolean approved = false;

        Mockito.when(bookingRepository.findByIdAndItemOwnerId(bookingId, userId))
                .thenReturn(Optional.of(booking));

        assertEquals(expectedDto, bookingService.bookingStatus(bookingId, userId, approved));
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void givenApproveWithNoBookingWithGivenIdAndOwnerId_whenApprove_thenThrowNotFoundException() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder().status(BookingStatus.WAITING);
        Booking booking = bookingBuilder.buildBooking();
        long bookingId = booking.getId();
        long userId = booking.getItem().getOwner().getId() + 1;
        boolean approved = true;

        Mockito.when(bookingRepository.findByIdAndItemOwnerId(bookingId, userId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.bookingStatus(bookingId, userId, approved));
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void givenApproveBookingWithNotWaitingStatus_whenApprove_thenThrowCustomValidationException() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder().status(BookingStatus.APPROVED);
        Booking booking = bookingBuilder.buildBooking();
        long bookingId = booking.getId();
        long userId = booking.getItem().getOwner().getId();

        Mockito.when(bookingRepository.findByIdAndItemOwnerId(bookingId, userId))
                .thenReturn(Optional.of(booking));

        assertThrows(CustomValidationException.class,
                () -> bookingService.bookingStatus(bookingId, userId, true));
        assertEquals(BookingStatus.APPROVED, booking.getStatus());

        bookingBuilder.status(BookingStatus.REJECTED);
        booking = bookingBuilder.buildBooking();
        Mockito.when(bookingRepository.findByIdAndItemOwnerId(bookingId, userId))
                .thenReturn(Optional.of(booking));

        assertThrows(CustomValidationException.class,
                () -> bookingService.bookingStatus(bookingId, userId, true));
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void givenFindByIdCorrect_whenFindById_thenReturnDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        long bookingId = booking.getId();
        long userId = booking.getBooker().getId();

        Mockito.when(bookingRepository.findByIdAndItemOwnerIdOrBookerId(bookingId, userId))
                .thenReturn(Optional.of(booking));

        assertEquals(responseDto, bookingService.getBookingById(bookingId, userId));
    }

    @Test
    void givenFindByIdWithNoBookingWithGivenIdAndOwnerOrBookerId_whenFindById_thenThrowNotFoundException() {
        long bookingId = 1L;
        long userId = 2L;

        Mockito.when(bookingRepository.findByIdAndItemOwnerIdOrBookerId(bookingId, userId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(bookingId, userId));
    }

    @Test
    void givenFindByBookerWithIncorrectBookerId_whenFindByBooker_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> bookingService.getAllBookingsByBooker(1L, BookingState.ALL, 0, 1));
    }

    @Test
    void givenFindByBookerWithSearchStatePastAndPagination_whenFindByBooker_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        long bookerId = booking.getBooker().getId();
        BookingState searchState = BookingState.PAST;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(booking.getBooker()));
        Mockito.lenient().when(bookingRepository.findByBookerAndEndIsBeforeOrderByStartDesc(
                        Mockito.any(User.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking));

        LocalDateTime minNow = LocalDateTime.now();
        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByBooker(bookerId, searchState, from, size));
        LocalDateTime maxNow = LocalDateTime.now();

        Mockito.verify(bookingRepository).findByBookerAndEndIsBeforeOrderByStartDesc(
                Mockito.argThat(booker -> Objects.nonNull(booker) &&
                        Objects.equals(booker.getId(), booking.getBooker().getId())),
                Mockito.argThat(now -> Objects.nonNull(now) &&
                        !now.isBefore(minNow) && !now.isAfter(maxNow)),
                Mockito.eq(PageRequest.of(0, 1, sort)));
    }

    @Test
    void givenFindByBookerWithSearchStateFutureAndPagination_whenFindByBooker_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        long bookerId = booking.getBooker().getId();
        BookingState searchState = BookingState.FUTURE;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(booking.getBooker()));
        Mockito.lenient().when(bookingRepository.findByBookerAndStartIsAfterOrderByStartDesc(
                        Mockito.any(User.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking));

        LocalDateTime minNow = LocalDateTime.now();
        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByBooker(bookerId, searchState, from, size));
        LocalDateTime maxNow = LocalDateTime.now();

        Mockito.verify(bookingRepository).findByBookerAndStartIsAfterOrderByStartDesc(
                Mockito.argThat(booker -> Objects.nonNull(booker) &&
                        Objects.equals(booker.getId(), booking.getBooker().getId())),
                Mockito.argThat(now -> Objects.nonNull(now) &&
                        !now.isBefore(minNow) && !now.isAfter(maxNow)),
                Mockito.eq(PageRequest.of(0, 1, sort)));
    }

    @Test
    void givenFindByBookerWithSearchStateCurrentAndPagination_whenFindByBooker_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        long bookerId = booking.getBooker().getId();
        BookingState searchState = BookingState.CURRENT;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(booking.getBooker()));
        Mockito.lenient().when(bookingRepository.findByBookerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                        Mockito.any(User.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking));

        LocalDateTime minNow = LocalDateTime.now();
        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByBooker(bookerId, searchState, from, size));
        LocalDateTime maxNow = LocalDateTime.now();

        Mockito.verify(bookingRepository).findByBookerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                Mockito.argThat(booker -> Objects.nonNull(booker) &&
                        Objects.equals(booker.getId(), booking.getBooker().getId())),
                Mockito.argThat(now -> Objects.nonNull(now) &&
                        !now.isBefore(minNow) && !now.isAfter(maxNow)),
                Mockito.argThat(now -> Objects.nonNull(now) &&
                        !now.isBefore(minNow) && !now.isAfter(maxNow)),
                Mockito.eq(PageRequest.of(0, 1, sort)));
    }

    @Test
    void givenFindByBookerWithSearchStateWaitingAndPagination_whenFindByBooker_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        long bookerId = booking.getBooker().getId();
        BookingState searchState = BookingState.WAITING;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(booking.getBooker()));
        Mockito.lenient().when(bookingRepository.findByBookerAndStatusIsOrderByStartDesc(
                        Mockito.argThat(booker -> Objects.nonNull(booker) &&
                                Objects.equals(booker.getId(), booking.getBooker().getId())),
                        Mockito.eq(BookingStatus.WAITING),
                        Mockito.eq(PageRequest.of(0, 1, sort))))
                .thenReturn(List.of(booking));

        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByBooker(bookerId, searchState, from, size));
    }

    @Test
    void givenFindByBookerWithSearchStateRejectedAndPagination_whenFindByBooker_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        long bookerId = booking.getBooker().getId();
        BookingState searchState = BookingState.REJECTED;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(booking.getBooker()));
        Mockito.lenient().when(bookingRepository.findByBookerAndStatusIsOrderByStartDesc(
                        Mockito.argThat(booker -> Objects.nonNull(booker) &&
                                Objects.equals(booker.getId(), booking.getBooker().getId())),
                        Mockito.eq(BookingStatus.REJECTED),
                        Mockito.eq(PageRequest.of(0, 1, sort))))
                .thenReturn(List.of(booking));

        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByBooker(bookerId, searchState, from, size));
    }

    @Test
    void givenFindByBookerWithSearchStateAllAndPagination_whenFindByBooker_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        long bookerId = booking.getBooker().getId();
        BookingState searchState = BookingState.ALL;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(booking.getBooker()));
        Mockito.lenient().when(bookingRepository.findByBookerOrderByStartDesc(
                        Mockito.argThat(booker -> Objects.nonNull(booker) &&
                                Objects.equals(booker.getId(), booking.getBooker().getId())),
                        Mockito.eq(PageRequest.of(0, 1, sort))))
                .thenReturn(List.of(booking));

        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByBooker(bookerId, searchState, from, size));
    }

    @Test
    void givenFindByOwnerWithIncorrectBookerId_whenFindByOwner_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> bookingService.getAllBookingsByBooker(1L, BookingState.ALL, 0, 1));
    }

    @Test
    void givenFindByOwnerWithSearchStatePastAndPagination_whenFindByOwner_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        User owner = booking.getItem().getOwner();
        long ownerId = owner.getId();
        BookingState searchState = BookingState.PAST;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        Mockito.lenient().when(bookingRepository.findByItemOwnerAndEndIsBeforeOrderByStartDesc(
                        Mockito.any(User.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking));

        LocalDateTime minNow = LocalDateTime.now();
        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByOwner(ownerId, searchState, from, size));
        LocalDateTime maxNow = LocalDateTime.now();

        Mockito.verify(bookingRepository).findByItemOwnerAndEndIsBeforeOrderByStartDesc(
                Mockito.argThat(user -> Objects.nonNull(user) &&
                        Objects.equals(user.getId(), owner.getId())),
                Mockito.argThat(now -> Objects.nonNull(now) &&
                        !now.isBefore(minNow) && !now.isAfter(maxNow)),
                Mockito.eq(PageRequest.of(0, 1, sort)));
    }

    @Test
    void givenFindByOwnerWithSearchStateFutureAndPagination_whenFindByOwner_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        User owner = booking.getItem().getOwner();
        long ownerId = owner.getId();
        BookingState searchState = BookingState.FUTURE;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        Mockito.lenient().when(bookingRepository.findByItemOwnerAndStartIsAfterOrderByStartDesc(
                        Mockito.any(User.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking));

        LocalDateTime minNow = LocalDateTime.now();
        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByOwner(ownerId, searchState, from, size));
        LocalDateTime maxNow = LocalDateTime.now();

        Mockito.verify(bookingRepository).findByItemOwnerAndStartIsAfterOrderByStartDesc(
                Mockito.argThat(user -> Objects.nonNull(user) &&
                        Objects.equals(user.getId(), owner.getId())),
                Mockito.argThat(now -> Objects.nonNull(now) &&
                        !now.isBefore(minNow) && !now.isAfter(maxNow)),
                Mockito.eq(PageRequest.of(0, 1, sort)));
    }

    @Test
    void givenFindByOwnerWithSearchStateCurrentAndPagination_whenFindByOwner_ThenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        User owner = booking.getItem().getOwner();
        long ownerId = owner.getId();
        BookingState searchState = BookingState.CURRENT;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        Mockito.lenient().when(bookingRepository.findByItemOwnerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                        Mockito.any(User.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking));

        LocalDateTime minNow = LocalDateTime.now();
        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByOwner(ownerId, searchState, from, size));
        LocalDateTime maxNow = LocalDateTime.now();

        Mockito.verify(bookingRepository).findByItemOwnerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                Mockito.argThat(user -> Objects.nonNull(user) &&
                        Objects.equals(user.getId(), owner.getId())),
                Mockito.argThat(now -> Objects.nonNull(now) &&
                        !now.isBefore(minNow) && !now.isAfter(maxNow)),
                Mockito.argThat(now -> Objects.nonNull(now) &&
                        !now.isBefore(minNow) && !now.isAfter(maxNow)),
                Mockito.eq(PageRequest.of(0, 1, sort)));
    }

    @Test
    void givenFindByOwnerWithSearchStateWaitingAndPagination_whenFindByOwner_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        User owner = booking.getItem().getOwner();
        long ownerId = owner.getId();
        BookingState searchState = BookingState.WAITING;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        Mockito.lenient().when(bookingRepository.findByItemOwnerAndStatusIsOrderByStartDesc(
                        Mockito.argThat(user -> Objects.nonNull(user) &&
                                Objects.equals(user.getId(), owner.getId())),
                        Mockito.eq(BookingStatus.WAITING),
                        Mockito.eq(PageRequest.of(0, 1, sort))))
                .thenReturn(List.of(booking));

        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByOwner(ownerId, searchState, from, size));
    }

    @Test
    void givenFindByOwnerWithSearchStateRejectedAndPagination_whenFindByOwner_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        User owner = booking.getItem().getOwner();
        long ownerId = owner.getId();
        BookingState searchState = BookingState.REJECTED;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        Mockito.lenient().when(bookingRepository.findByItemOwnerAndStatusIsOrderByStartDesc(
                        Mockito.argThat(user -> Objects.nonNull(user) &&
                                Objects.equals(user.getId(), owner.getId())),
                        Mockito.eq(BookingStatus.REJECTED),
                        Mockito.eq(PageRequest.of(0, 1, sort))))
                .thenReturn(List.of(booking));

        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByOwner(ownerId, searchState, from, size));
    }

    @Test
    void givenFindByOwnerWithSearchStateAllAndPagination_whenFindByOwner_thenReturnListOfDto() {
        TestBookingBuilder bookingBuilder = TestBookingBuilder.defaultBuilder();
        Booking booking = bookingBuilder.buildBooking();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        User owner = booking.getItem().getOwner();
        long ownerId = owner.getId();
        BookingState searchState = BookingState.ALL;
        long from = 0;
        int size = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        Mockito.lenient().when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        Mockito.lenient().when(bookingRepository.findByItemOwnerOrderByStartDesc(
                        Mockito.argThat(user -> Objects.nonNull(user) &&
                                Objects.equals(user.getId(), owner.getId())),
                        Mockito.eq(PageRequest.of(0, 1, sort))))
                .thenReturn(List.of(booking));

        assertEquals(List.of(responseDto),
                bookingService.getAllBookingsByOwner(ownerId, searchState, from, size));
    }
}