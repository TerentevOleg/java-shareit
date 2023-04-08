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
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

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
    }

    @BeforeEach
    void setUp() {
        itemRequestBuilder = TestItemRequestBuilder.defaultBuilder();
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

        verify(itemRequestClient, never()).add(any(), anyLong());
    }

    @Test
    void givenFindAllByOtherUsersWithNegativeFrom_whenFind_thenStatusBadRequest() throws Exception {
        long userId = 1000L;

        mvc.perform(get("/requests/all")
                        .param("from", "-1")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).findByOtherUsers(anyLong(), anyLong(), anyInt());
    }

    @Test
    void givenFindAllByOtherUsersWithNegativeSize_whenFind_thenStatusBadRequest() throws Exception {
        long userId = 1000L;

        mvc.perform(get("/requests/all")
                        .param("size", "-1")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).findByOtherUsers(anyLong(), anyLong(), anyInt());
    }

    @Test
    void givenFindAllByOtherUsersWithZeroSize_whenFind_thenStatusBadRequest() throws Exception {
        long userId = 1000L;

        mvc.perform(get("/requests/all")
                        .param("size", "0")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).findByOtherUsers(anyLong(), anyLong(), anyInt());
    }
}