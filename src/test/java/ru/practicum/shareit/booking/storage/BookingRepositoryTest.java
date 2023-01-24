package ru.practicum.shareit.booking.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingBuilderTestUtils;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemBuilderTestUtil;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserBuildersTestUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void findByIdAndItemOwnerIdOrBookerId() {
        UserBuildersTestUtil userBuilder = UserBuildersTestUtil.defaultBuilder();
        User user1 = userBuilder.email("user1@mail.ru").userBuild();
        User user2 = userBuilder.email("user2@mail.ru").userBuild();
        User user3 = userBuilder.email("user3@mail.ru").userBuild();
        User user4 = userBuilder.email("user4@mail.ru").userBuild();
        testEntityManager.persistAndGetId(user1);
        testEntityManager.persistAndGetId(user2);
        testEntityManager.persistAndGetId(user3);
        testEntityManager.persistAndGetId(user4);
        ItemBuilderTestUtil itemBuilder = ItemBuilderTestUtil.defaultBuilder();
        Item item1 = itemBuilder.owner(user1).itemBuilder();
        Item item2 = itemBuilder.owner(user2).itemBuilder();
        testEntityManager.persistAndGetId(item1);
        testEntityManager.persistAndGetId(item2);
        BookingBuilderTestUtils bookingBuilder = BookingBuilderTestUtils.defaultBuilder();
        Booking booking1 = bookingBuilder.item(item1).booker(user3).bookingBuild();
        Booking booking2 = bookingBuilder.item(item2).booker(user4).bookingBuild();
        testEntityManager.persistAndGetId(booking1);
        testEntityManager.persistAndGetId(booking2);

        Optional<Booking> queryResult =
                bookingRepository.findByIdAndItemOwnerIdOrBookerId(booking1.getId(), user1.getId());
        assertTrue(queryResult.isPresent());
        assertEquals(booking1.getId(), queryResult.get().getId());

        queryResult =
                bookingRepository.findByIdAndItemOwnerIdOrBookerId(booking1.getId(), user3.getId());
        assertTrue(queryResult.isPresent());
        assertEquals(booking1.getId(), queryResult.get().getId());

        queryResult =
                bookingRepository.findByIdAndItemOwnerIdOrBookerId(booking1.getId(), user2.getId());
        assertTrue(queryResult.isEmpty());
    }
}