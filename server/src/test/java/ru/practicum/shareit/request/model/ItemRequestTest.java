package ru.practicum.shareit.request.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestTest {
    @Test
    void testEquals() {
        ItemRequest request1 = new ItemRequest();
        ItemRequest request2 = new ItemRequest();

        assertEquals(request1, request1);
        assertNotEquals(request1, request2);

        request1.setId(1L);
        request1.setDescription("description1");
        request2.setId(1L);
        request2.setDescription("description2");

        assertEquals(request1, request2);

        request2.setId(2L);

        assertNotEquals(request1, request2);
    }
}