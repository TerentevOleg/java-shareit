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
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

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

        public BookingDtoResponse buildDtoResponse() {
            return new BookingDtoResponse(
                    id,
                    start,
                    end,
                    new ItemDto(itemId, itemName, itemDescription, itemAvailable, id),
                    new UserDto(bookerId, bookerName, bookerEmail),
                    status
            );
        }
    }

    @BeforeEach
    void setUp() {
        bookingBuilder = TestBookingBuilder.defaultBuilder();
    }

    @Test
    void givenAddValidDto_whenAddDto_thenStatusOkAndJsonBody() throws Exception {
        BookingDtoRequest requestDto = bookingBuilder.buildDtoRequest();
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();

        when(bookingService.add(requestDto, bookingBuilder.bookerId()))
                .thenReturn(responseDto);

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingBuilder.buildDtoResponse())));

        verify(bookingService, times(1)).add(any(), anyLong());
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

        verify(bookingService, never()).add(any(), anyLong());
    }

    @Test
    void givenAddDtoWithNullStart_whenAddDto_thenStatusBadRequest() throws Exception {
        bookingBuilder.start(null);

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingBuilder.buildDtoRequest()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).add(any(), anyLong());
    }

    @Test
    void givenAddDtoWithNullEnd_whenAddDto_thenStatusBadRequest() throws Exception {
        bookingBuilder.end(null);

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingBuilder.buildDtoRequest()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).add(any(), anyLong());
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

        verify(bookingService, never()).add(any(), anyLong());
    }

    @Test
    void givenApproveCorrect_whenApprove_thenStatusOkAndJsonBody() throws Exception {
        boolean approved = true;
        bookingBuilder.status(BookingStatus.APPROVED);

        when(bookingService.bookingStatus(bookingBuilder.id, bookingBuilder.bookerId(), approved))
                .thenReturn(bookingBuilder.buildDtoResponse());

        mvc.perform(patch("/bookings/{bookingId}", bookingBuilder.id)
                        .param("approved", String.valueOf(approved))
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingBuilder.buildDtoResponse())));
    }

    @Test
    void givenFindById_whenFindById_thenStatusOkAndJsonBody() throws Exception {
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();

        when(bookingService.getBookingById(bookingBuilder.id, bookingBuilder.bookerId))
                .thenReturn(responseDto);

        mvc.perform(get("/bookings/{bookingId}", bookingBuilder.id)
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));

        verify(bookingService, times(1)).getBookingById(anyLong(), anyLong());
    }

    @Test
    void givenFindByBookerCorrect_whenFindByBooker_thenStatusOkAndJsonArrayBody() throws Exception {
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        BookingState searchState = BookingState.WAITING;
        long from  = 10L;
        Integer size = 100;

        when(bookingService.getAllBookingsByBooker(bookingBuilder.bookerId, searchState, from, size))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/bookings")
                        .param("state", searchState.toString())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(responseDto))));

        verify(bookingService, times(1)).getAllBookingsByBooker(anyLong(), any(),
                anyLong(), anyInt());
    }

    @Test
    void givenFindByBookerWithoutPaginationParams_whenFindByBooker_thenStatusOkAndJsonArrayBody() throws Exception {
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        BookingState searchState = BookingState.WAITING;

        when(bookingService.getAllBookingsByBooker(bookingBuilder.bookerId, searchState, 0, 10))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/bookings")
                        .param("state", searchState.toString())
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(responseDto))));

        verify(bookingService, times(1)).getAllBookingsByBooker(anyLong(), any(),
                anyLong(), anyInt());
    }

    @Test
    void givenFindByBookerWithIncorrectSearchState_whenFindByBooker_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "unknown_state")
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByBooker(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByBookerWithNegativeFrom_whenFindByBooker_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", BookingState.WAITING.toString())
                        .param("from", "-1")
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByBooker(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByBookerWithNegativeSize_whenFindByBooker_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", BookingState.WAITING.toString())
                        .param("size", "-1")
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByBooker(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByBookerWithZeroSize_whenFindByBooker_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", BookingState.WAITING.toString())
                        .param("size", "0")
                        .header("X-Sharer-User-Id", bookingBuilder.bookerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByBooker(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerCorrect_whenFindByOwner_thenStatusOkAndJsonArrayBody() throws Exception {
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        BookingState searchState = BookingState.WAITING;
        long from  = 10L;
        Integer size = 100;

        when(bookingService.getAllBookingsByOwner(bookingBuilder.itemOwnerId(), searchState, from, size))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/bookings/owner")
                        .param("state", searchState.toString())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(responseDto))));

        verify(bookingService, times(1)).getAllBookingsByOwner(anyLong(), any(),
                anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerWithoutPaginationParams_whenFindByOwner_thenStatusOkAndJsonArrayBody() throws Exception {
        BookingDtoResponse responseDto = bookingBuilder.buildDtoResponse();
        BookingState searchState = BookingState.WAITING;

        when(bookingService.getAllBookingsByOwner(bookingBuilder.itemOwnerId(), searchState, 0, 10))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/bookings/owner")
                        .param("state", searchState.toString())
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(responseDto))));

        verify(bookingService, times(1)).getAllBookingsByOwner(anyLong(), any(),
                anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerWithIncorrectSearchState_whenFindByOwner_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "unknown_state")
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerWithNegativeFrom_whenFindByOwner_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", BookingState.WAITING.toString())
                        .param("from", "-1")
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerWithNegativeSize_whenFindByOwner_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", BookingState.WAITING.toString())
                        .param("size", "-1")
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void givenFindByOwnerWithZeroSize_whenFindByOwner_thenStatusBadResponse() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", BookingState.WAITING.toString())
                        .param("size", "0")
                        .header("X-Sharer-User-Id", bookingBuilder.itemOwnerId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
    }
}