package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private TestItemRequestBuilder itemRequestBuilder;

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    private static class TestItemRequestBuilder {
        private Long id = 1L;
        private String description = "description";
        private LocalDateTime created = LocalDateTime.of(1993, 12, 3, 10, 1);
        private Long itemId = 11L;
        private String itemName = "itemName";
        private String itemDescription = "itemDescription";
        private Boolean itemAvailable = true;
        private Long itemRequestId = 21L;

        public ItemRequestDtoRequest buildDtoRequest() {
            return new ItemRequestDtoRequest(description);
        }

        public ItemRequestDtoResponse buildDtoResponse() {
            return new ItemRequestDtoResponse(id, description, created);
        }

        public ItemRequestDto buildDto() {
            return new ItemRequestDto(id, description, created,
                    List.of(new ItemDto(itemId, itemName, itemDescription, itemAvailable, itemRequestId)));
        }
    }

    @BeforeEach
    void setUp() {
        itemRequestBuilder = TestItemRequestBuilder.defaultBuilder();
    }

    @Test
    void givenAddWithValidDto_whenAdd_thenStatusOkAndJsonBody() throws Exception {
        long userId = 1000L;
        ItemRequestDtoRequest requestDto = itemRequestBuilder.buildDtoRequest();
        ItemRequestDtoResponse responseDto = itemRequestBuilder.buildDtoResponse();

        when(itemRequestService.add(requestDto, userId))
                .thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));

        verify(itemRequestService, times(1)).add(any(), anyLong());
    }

    @Test
    void givenAddWithBlankDescription_whenAdd_thenStatusBadRequest() throws Exception {
        long userId = 1000L;
        itemRequestBuilder.description("   ");
        ItemRequestDtoRequest requestDto = itemRequestBuilder.buildDtoRequest();

        mvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).add(any(), anyLong());
    }

    @Test
    void givenFindByUser_whenFind_thenStatusOkAndJsonArrayBody() throws Exception {
        long userId = 1000L;
        ItemRequestDto requestDto = itemRequestBuilder.buildDto();

        when(itemRequestService.findByRequester(userId))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(requestDto))));

        verify(itemRequestService, times(1)).findByRequester(anyLong());
    }

    @Test
    void givenFindAllByOtherUsersWithPagination_whenFind_thenStatusOkAndJsonArrayBody() throws Exception {
        long userId = 1000L;
        long from = 10L;
        Integer size = 100;
        ItemRequestDto requestDto = itemRequestBuilder.buildDto();

        when(itemRequestService.findByOtherUsers(userId, from, size))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", String.valueOf(userId))
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(requestDto))));

        verify(itemRequestService, times(1)).findByOtherUsers(anyLong(), anyLong(), anyInt());
    }

    @Test
    void givenFindAllByOtherUsersWithoutPaginationParams_whenFind_thenStatusOkAndJsonArrayBody() throws Exception {
        long userId = 1000L;
        ItemRequestDto requestDto = itemRequestBuilder.buildDto();

        when(itemRequestService.findByOtherUsers(userId, 0, 10))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(requestDto))));

        verify(itemRequestService, times(1)).findByOtherUsers(anyLong(), anyLong(), anyInt());
    }

    @Test
    void givenFindAllByOtherUsersWithNegativeFrom_whenFind_thenStatusBadRequest() throws Exception {
        long userId = 1000L;

        mvc.perform(get("/requests/all")
                        .param("from", "-1")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).findByOtherUsers(anyLong(), anyLong(), anyInt());
    }

    @Test
    void givenFindAllByOtherUsersWithNegativeSize_whenFind_thenStatusBadRequest() throws Exception {
        long userId = 1000L;

        mvc.perform(get("/requests/all")
                        .param("size", "-1")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).findByOtherUsers(anyLong(), anyLong(), anyInt());
    }

    @Test
    void givenFindAllByOtherUsersWithZeroSize_whenFind_thenStatusBadRequest() throws Exception {
        long userId = 1000L;

        mvc.perform(get("/requests/all")
                        .param("size", "0")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).findByOtherUsers(anyLong(), anyLong(), anyInt());
    }

    @Test
    void givenFindById_whenFind_thenStatusOkAndJsonBody() throws Exception {
        long userId = 1000L;
        long requestId = itemRequestBuilder.id();
        ItemRequestDto expectedDto = itemRequestBuilder.buildDto();

        when(itemRequestService.findById(requestId, userId))
                .thenReturn(expectedDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedDto)));

        verify(itemRequestService, times(1)).findById(anyLong(), anyLong());
    }
}