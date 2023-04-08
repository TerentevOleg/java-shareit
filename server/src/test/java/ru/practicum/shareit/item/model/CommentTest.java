package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {
    @Test
    void testEquals() {
        Comment comment1 = new Comment();
        Comment comment2 = new Comment();

        assertEquals(comment1, comment1);
        assertNotEquals(comment1, comment2);

        comment1.setId(1L);
        comment1.setText("text1");
        comment2.setId(1L);
        comment2.setText("text2");

        assertEquals(comment1, comment2);

        comment2.setId(2L);

        assertNotEquals(comment1, comment2);
    }
}