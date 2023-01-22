package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void testEquals() {
        User user1 = new User();
        User user2 = new User();

        assertEquals(user1, user1);
        assertNotEquals(user1, user2);

        user1.setId(1L);
        user1.setName("name1");
        user2.setId(1L);
        user2.setName("name2");
        assertEquals(user1, user2);

        user2.setId(2L);
        assertNotEquals(user1, user2);
    }
}