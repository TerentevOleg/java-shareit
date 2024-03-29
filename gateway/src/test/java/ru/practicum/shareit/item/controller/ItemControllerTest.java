package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private MockMvc mvc;

    private TestCommentBuilder commentBuilder;

    private TestItemBuilder itemBuilder;

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    private static class TestItemBuilder {
        private Long id = 1L;
        private String name = "name";
        private String description = "description";
        private Boolean available = true;
        private Long requestId = 10L;
        private Long lastBookingId = 20L;
        private LocalDateTime lastBookingStart = LocalDateTime.now().minusDays(4);
        private LocalDateTime lastBookingEnd = LocalDateTime.now().minusDays(3);
        private Long lastBookingBookerId = 30L;
        private BookingStatus lastBookingStatus = BookingStatus.APPROVED;
        private Long nextBookingId = 40L;
        private LocalDateTime nextBookingStart = LocalDateTime.now().plusDays(3);
        private LocalDateTime nextBookingEnd = LocalDateTime.now().plusDays(4);
        private Long nextBookingBookerId = 50L;
        private BookingStatus nextBookingStatus = BookingStatus.APPROVED;
        @Setter(AccessLevel.NONE)
        private TestCommentBuilder commentBuilder = TestCommentBuilder.defaultBuilder();

        public ItemDto buildDto() {
            return new ItemDto(id, name, description, available, requestId);
        }
    }

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    private static class TestCommentBuilder {
        private Long id = 100L;
        private String text = "comment";
        private String authorName = "commentAuthorName";
        private LocalDateTime created = LocalDateTime.of(1993, 12, 3, 10, 1);

        public CommentDtoRequest buildDtoRequest() {
            return new CommentDtoRequest(text);
        }
    }

    @BeforeEach
    void setUp() {
        itemBuilder = TestItemBuilder.defaultBuilder();
        commentBuilder = TestCommentBuilder.defaultBuilder();
    }

    @Test
    void givenGetByIdWithoutUserHeader_whenGetById_thenStatusBadRequest() throws Exception {
        mvc.perform(get("/items/{id}", itemBuilder.id()))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getById(anyLong(), anyLong());
    }

    @Test
    void getAllWithNegativeFromAndThenStatusBadRequest() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", String.valueOf(1001L))
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getAll(anyLong(), anyLong(), anyInt());
    }

    @Test
    void getAllWithNegativeSizeAndThenStatusBadRequest() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", String.valueOf(1001L))
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getAll(anyLong(), anyLong(), anyInt());
    }

    @Test
    void getAllWithZeroSizeAndThenStatusBadRequest() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", String.valueOf(1001L))
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getAll(anyLong(), anyLong(), anyInt());
    }

    @Test
    void givenSearchWithNegativeFrom_whenSearch_thenStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "text")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).search(any(), anyLong(), anyInt());
    }

    @Test
    void givenSearchWithNegativeSize_whenSearch_thenStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "text")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).search(any(), anyLong(), anyInt());
    }

    @Test
    void givenSearchWithZeroSize_whenSearch_thenStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "text")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).search(any(), anyLong(), anyInt());
    }

    @Test
    void givenAddDtoWithBlankName_whenAddDto_thenStatusBadRequest() throws Exception {
        long userId = 1001L;
        itemBuilder.id(null).name("   ");
        ItemDto requestDto = itemBuilder.buildDto();

        mvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(any(), anyLong());
    }

    @Test
    void givenAddDtoWithBlankDescription_whenAddDto_thenStatusBadRequest() throws Exception {
        long userId = 1000L;
        itemBuilder.id(null).description("   ");
        ItemDto requestDto = itemBuilder.buildDto();

        mvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(any(), anyLong());
    }

    @Test
    void givenAddDtoWithNullAvailable_whenAddDto_thenStatusBadRequest() throws Exception {
        long userId = 1000L;
        itemBuilder.id(null).available(null);
        ItemDto requestDto = itemBuilder.buildDto();

        mvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(any(), anyLong());
    }

    @Test
    void givenPatchWithBlankName_whenPatch_thenStatusOkAndJsonBody() throws Exception {
        itemBuilder.name("  ");
        long itemId = itemBuilder.id();
        long userId = 1000L;
        ItemPatchDto patchDto = new ItemPatchDto(itemBuilder.name(), null, null);

        mvc.perform(patch("/items/{id}", itemId)
                        .content(objectMapper.writeValueAsString(patchDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).patch(anyLong(), any(), anyLong());
    }

    @Test
    void givenPatchWithBlankDescription_whenPatch_thenStatusOkAndJsonBody() throws Exception {
        itemBuilder.description("  ");
        long itemId = itemBuilder.id();
        long userId = 1001L;
        ItemPatchDto patchDto = new ItemPatchDto(null, itemBuilder.description, null);

        mvc.perform(patch("/items/{id}", itemId)
                        .content(objectMapper.writeValueAsString(patchDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).patch(anyLong(), any(), anyLong());
    }

    @Test
    void givenAddCommentWithBlankText_whenAddComment_thenStatusBadRequest() throws Exception {
        commentBuilder.text("   ");
        CommentDtoRequest inputDto = commentBuilder.buildDtoRequest();
        long itemId = 1000L;
        long userId = 1100L;

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(any(), anyLong(), anyLong());
    }
}