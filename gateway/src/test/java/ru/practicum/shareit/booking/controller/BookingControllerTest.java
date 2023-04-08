package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private MockMvc mvc;

    private TestBookingBuilder bookingBuilder;

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    private static class TestBookingBuilder {
        private Long id = 1L;
        private LocalDateTime start = LocalDateTime.now().plusDays(2);
        private LocalDateTime end = LocalDateTime.now().plusDays(4);
        private BookingStatus status = BookingStatus.WAITING;
        private Long itemId = 10L;
        private String itemName = "itemName";
        private String itemDescription = "itemDescription";
        private Boolean itemAvailable = true;
        private Long itemOwnerId = 20L;
        private Long bookerId = 30L;
        private String bookerName = "bookerName";
        private String bookerEmail = "booker@mail.ru";

        public BookingDtoRequest buildDtoRequest() {
            return new BookingDtoRequest(start, end, itemId);
        }
    }

    @BeforeEach
    void setUp() {
        bookingBuilder = TestBookingBuilder.defaultBuilder();
    }

    @Test
    void givenAddDtoWithStartInPast_whenAddDto_thenStatusBadRequest() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        bookingBuilder.start(now.minusDays(2)).end(now.plusDays(2));

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingBuilder.buildDtoRequest()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).add(any(), anyLong());
    }

    @Test
    void givenAddDtoWithNullItemId_whenAddDto_thenStatusBadRequest() throws Exception {
        bookingBuilder.itemId(null);

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingBuilder.buildDtoRequest()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).add(any(), anyLong());
    }

    @Test
    void givenFindByBookerWithIncorrectSearchState_whenFindByBooker_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "unknown_state")
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsByBooker(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByBookerWithNegativeFrom_whenFindByBooker_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", BookingState.WAITING.toString())
                        .param("from", "-1")
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsByBooker(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByBookerWithNegativeSize_whenFindByBooker_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", BookingState.WAITING.toString())
                        .param("size", "-1")
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsByBooker(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByBookerWithZeroSize_whenFindByBooker_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", BookingState.WAITING.toString())
                        .param("size", "0")
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsByBooker(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerWithIncorrectSearchState_whenFindByOwner_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "unknown_state")
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerWithNegativeFrom_whenFindByOwner_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", BookingState.WAITING.toString())
                        .param("from", "-1")
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerWithNegativeSize_whenFindByOwner_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", BookingState.WAITING.toString())
                        .param("size", "-1")
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerWithZeroSize_whenFindByOwner_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", BookingState.WAITING.toString())
                        .param("size", "0")
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
    }
}