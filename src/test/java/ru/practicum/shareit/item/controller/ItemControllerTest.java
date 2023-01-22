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
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

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

        public ItemDtoResponse buildDtoResponse() {
            return new ItemDtoResponse(id, name, description, available, requestId,
                    new BookingDtoShort(lastBookingId, lastBookingStart, lastBookingEnd,
                            lastBookingBookerId, lastBookingStatus),
                    new BookingDtoShort(nextBookingId, nextBookingStart, nextBookingEnd,
                            nextBookingBookerId, nextBookingStatus),
                    List.of(commentBuilder.buildDtoResponse()));
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

        public CommentDtoResponse buildDtoResponse() {
            return new CommentDtoResponse(id, text, authorName, created);
        }
    }

    @BeforeEach
    void setUp() {
        itemBuilder = TestItemBuilder.defaultBuilder();
        commentBuilder = TestCommentBuilder.defaultBuilder();
    }

    @Test
    void givenGetById_whenGetById_thenStatusOkAndJsonBody() throws Exception {
        Long itemId = itemBuilder.id();
        long userId = 1000L;
        ItemDtoResponse responseDto = itemBuilder.buildDtoResponse();

        when(itemService.getById(itemId, userId))
                .thenReturn(responseDto);

        mvc.perform(get("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void givenGetByIdWithoutUserHeader_whenGetById_thenStatusBadRequest() throws Exception {
        mvc.perform(get("/items/{id}", itemBuilder.id()))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getById(anyLong(), anyLong());
    }

    @Test
    void givenGetAllWithPaginationParams_whenGetAll_thenStatusOkAndJsonArrayBody() throws Exception {
        long userId = 1000L;
        ItemDtoResponse responseDto = itemBuilder.buildDtoResponse();
        long from = 10;
        Integer size = 100;

        when(itemService.getAll(userId, from, size))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", String.valueOf(userId))
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(responseDto))));
    }

    @Test
    void givenGetAllWithoutPaginationParams_whenGetAll_thenStatusOkAndJsonArrayBody() throws Exception {
        long userId = 1000L;

        ItemDtoResponse responseDto = itemBuilder.buildDtoResponse();

        when(itemService.getAll(userId, 0, 10))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(responseDto))));
    }

    @Test
    void getAllWithNegativeFromAndThenStatusBadRequest() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", String.valueOf(1001L))
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getAll(anyLong(), anyLong(), anyInt());
    }

    @Test
    void getAllWithNegativeSizeAndThenStatusBadRequest() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", String.valueOf(1001L))
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getAll(anyLong(), anyLong(), anyInt());
    }

    @Test
    void getAllWithZeroSizeAndThenStatusBadRequest() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", String.valueOf(1001L))
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getAll(anyLong(), anyLong(), anyInt());
    }

    @Test
    void givenSearchWithPagination_whenSearch_thenStatusOkAndJsonBody() throws Exception {
        String text = "text";
        long from = 10L;
        Integer size = 100;

        when(itemService.search(text, from, size))
                .thenReturn(List.of(itemBuilder.buildDto()));

        mvc.perform(get("/items/search")
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(itemBuilder.buildDto()))));
    }

    @Test
    void givenSearchWithoutPaginationParams_whenSearch_thenStatusOkAndJsonBody() throws Exception {
        String text = "text";

        when(itemService.search(text, 0, 10))
                .thenReturn(List.of(itemBuilder.buildDto()));

        mvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(itemBuilder.buildDto()))));
    }

    @Test
    void givenSearchWithBlankText_whenSearch_thenStatusOkAndEmptyJsonArrayBody() throws Exception {
        String text = "   ";

        mvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Collections.emptyList())));

        verify(itemService, never()).search(any(), anyLong(), anyInt());
    }

    @Test
    void givenSearchWithNegativeFrom_whenSearch_thenStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "text")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).search(any(), anyLong(), anyInt());
    }

    @Test
    void givenSearchWithNegativeSize_whenSearch_thenStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "text")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).search(any(), anyLong(), anyInt());
    }

    @Test
    void givenSearchWithZeroSize_whenSearch_thenStatusBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "text")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).search(any(), anyLong(), anyInt());
    }

    @Test
    void givenAddValidDto_whenAddDto_ThenStatusOkAndJsonBody() throws Exception {
        long userId = 1000L;
        ItemDto responseDto = itemBuilder.buildDto();
        itemBuilder.id(null);
        ItemDto requestDto = itemBuilder.buildDto();

        when(itemService.add(requestDto, userId))
                .thenReturn(responseDto);

        mvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
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

        verify(itemService, never()).add(any(), anyLong());
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

        verify(itemService, never()).add(any(), anyLong());
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

        verify(itemService, never()).add(any(), anyLong());
    }

    @Test
    void givenPatchWithAvailable_whenPatch_thenStatusOkAndJsonBody() throws Exception {
        itemBuilder.available(true);
        long itemId = itemBuilder.id();
        long userId = 1001L;
        ItemDto responseDto = itemBuilder.buildDto();
        ItemPatchDto patchDto = new ItemPatchDto(null, null, itemBuilder.available());

        when(itemService.patch(itemId, patchDto, userId))
                .thenReturn(responseDto);

        mvc.perform(patch("/items/{id}", itemId)
                        .content(objectMapper.writeValueAsString(patchDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void givenPatchWithValidName_whenPatch_thenStatusOkAndJsonBody() throws Exception {
        itemBuilder.name("name");
        long itemId = itemBuilder.id();
        long userId = 1001L;
        ItemDto responseDto = itemBuilder.buildDto();
        ItemPatchDto patchDto = new ItemPatchDto(itemBuilder.name(), null, null);

        when(itemService.patch(itemId, patchDto, userId))
                .thenReturn(responseDto);

        mvc.perform(patch("/items/{id}", itemId)
                        .content(objectMapper.writeValueAsString(patchDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));

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

        verify(itemService, never()).patch(anyLong(), any(), anyLong());
    }

    @Test
    void givenPatchWithValidDescription_whenPatch_thenStatusOkAndJsonBody() throws Exception {
        itemBuilder.description("description");
        long itemId = itemBuilder.id();
        long userId = 1001L;
        ItemDto responseDto = itemBuilder.buildDto();
        ItemPatchDto patchDto = new ItemPatchDto(null, itemBuilder.description(), null);

        when(itemService.patch(itemId, patchDto, userId))
                .thenReturn(responseDto);

        mvc.perform(patch("/items/{id}", itemId)
                        .content(objectMapper.writeValueAsString(patchDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));

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

        verify(itemService, never()).patch(anyLong(), any(), anyLong());
    }

    @Test
    void givenAddCommentWithValidDto_whenAddComment_thenStatusOkAndJsonBody() throws Exception {
        CommentDtoRequest requestDto = commentBuilder.buildDtoRequest();
        CommentDtoResponse responseDto = commentBuilder.buildDtoResponse();
        long itemId = 1000L;
        long userId = 1100L;

        when(itemService.addComment(requestDto, itemId, userId))
                .thenReturn(responseDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
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

        verify(itemService, never()).addComment(any(), anyLong(), anyLong());
    }
}