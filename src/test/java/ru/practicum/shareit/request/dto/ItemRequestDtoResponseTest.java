package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoResponseTest {
    @Autowired
    JacksonTester<ItemRequestDtoResponse> jacksonTester;

    private final ItemRequestDtoResponse itemRequestDtoResponse = new ItemRequestDtoResponse(1L,
            "description",
            LocalDateTime.of(1993, 12, 3, 10, 5));

    private final String json = "{\n" +
            "  \"id\": 1,\n" +
            "  \"description\": \"description\",\n" +
            "  \"created\": \"1993-12-03T10:05:00\"\n" +
            "}";

    @Test
    void itemRequestDtoResponseSerializationTest() throws IOException {
        assertThat(jacksonTester.write(itemRequestDtoResponse))
                .isEqualToJson(json, JSONCompareMode.STRICT);
    }

    @Test
    void itemRequestDtoResponseDeserializationTest() throws IOException {
        assertThat(jacksonTester.parse(json))
                .usingRecursiveComparison()
                .isEqualTo(itemRequestDtoResponse);
    }
}