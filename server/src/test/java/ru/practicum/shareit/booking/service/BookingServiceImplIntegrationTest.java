package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingBuilderTestUtils;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemBuilderTestUtil;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserBuildersTestUtil;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingServiceImplIntegrationTest {

    private final BookingService bookingService;

    private final UserService userService;

    private final EntityManager entityManager;

    @Test
    void getAllBookingsByBooker() {
        List<User> users = List.of(
                UserBuildersTestUtil.all(null, "name1", "user1@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name2", "user2@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name3", "user3@mail.ru").userBuild()
        );

        ItemBuilderTestUtil itemBuilderTestUtil = ItemBuilderTestUtil.defaultBuilder();
        List<Item> items = List.of(
                itemBuilderTestUtil.name("itemName1").description("description1")
                        .owner(users.get(0)).itemBuilder(),
                itemBuilderTestUtil.name("itemName2").description("description2")
                        .owner(users.get(1)).itemBuilder(),
                itemBuilderTestUtil.name("itemName3").description("description3")
                        .owner(users.get(2)).itemBuilder()
        );

        List<Booking> bookings = List.of(
                BookingBuilderTestUtils.all(null,
                        LocalDateTime.of(1993, 12, 3, 10, 5),
                        LocalDateTime.of(1994, 12, 3, 10, 5),
                        items.get(0), users.get(0), APPROVED).bookingBuild()
        );

        users.forEach(entityManager::persist);
        items.forEach(entityManager::persist);
        bookings.forEach(entityManager::persist);

        Long userId = users.get(0).getId();

        List<BookingDtoResponse> bookingDtoResponses = bookingService.getAllBookingsByBooker(userId,
                BookingState.ALL, 1, 10);

        assertEquals(1, bookingDtoResponses.size());
    }

    @Test
    void getBookingById() {
        List<User> users = List.of(
                UserBuildersTestUtil.all(null, "name1", "user1@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name2", "user2@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name3", "user3@mail.ru").userBuild()
        );

        ItemBuilderTestUtil itemBuilderTestUtil = ItemBuilderTestUtil.defaultBuilder();
        List<Item> items = List.of(
                itemBuilderTestUtil.name("itemName1").description("description1")
                        .owner(users.get(0)).itemBuilder(),
                itemBuilderTestUtil.name("itemName2").description("description2")
                        .owner(users.get(1)).itemBuilder(),
                itemBuilderTestUtil.name("itemName3").description("description3")
                        .owner(users.get(2)).itemBuilder()
        );

        List<Booking> bookings = List.of(
                BookingBuilderTestUtils.all(null,
                        LocalDateTime.of(1993, 12, 3, 10, 5),
                        LocalDateTime.of(1994, 12, 3, 10, 5),
                        items.get(0), users.get(0), APPROVED).bookingBuild()
        );

        users.forEach(entityManager::persist);
        items.forEach(entityManager::persist);
        bookings.forEach(entityManager::persist);

        Long id = bookings.get(0).getId();
        Long userId = users.get(0).getId();

        UserDto userDto = userService.getById(userId);
        BookingDtoResponse bookingDtoResponse = bookingService.getBookingById(id, userId);

        assertThat(bookingDtoResponse.getId()).isEqualTo(1);
        assertThat(bookingDtoResponse.getBooker()).isEqualTo(userDto);
        assertThat(bookingDtoResponse.getStatus()).isEqualTo(APPROVED);
        assertThat(bookingDtoResponse.getStart())
                .isEqualTo(LocalDateTime.of(1993, 12, 3, 10, 5));
        assertThat(bookingDtoResponse.getEnd())
                .isEqualTo(LocalDateTime.of(1994, 12, 3, 10, 5));
    }
}
