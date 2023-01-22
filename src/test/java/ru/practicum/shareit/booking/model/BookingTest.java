package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {
    @Test
    void testEquals() {
        Booking booking1 = new Booking();
        Booking booking2 = new Booking();

        assertEquals(booking1, booking1);
        assertNotEquals(booking1, booking2);

        booking1.setId(1L);
        booking1.setStatus(BookingStatus.APPROVED);
        booking2.setId(1L);
        booking1.setStatus(BookingStatus.REJECTED);

        assertEquals(booking1, booking2);

        booking2.setId(2L);
        assertNotEquals(booking1, booking2);
    }
}