package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoResponseTest {
    @Autowired
    private JacksonTester<CommentDtoResponse> jacksonTester;

    private final String json = "{\n" +
            "  \"id\": 1,\n" +
            "  \"text\": \"text\",\n" +
            "  \"authorName\": \"Author\",\n" +
            "  \"created\": \"1993-12-03T10:05:00\"\n" +
            "}";

    private final CommentDtoResponse commentDtoResponse = new CommentDtoResponse(1L,
            "text", "Author",
            LocalDateTime.of(1993, 12, 3, 10, 5));

    @Test
    void commentDtoResponseSerializationTest() throws IOException {
        assertThat(jacksonTester.write(commentDtoResponse))
                .isEqualToJson(json, JSONCompareMode.STRICT);
    }

    @Test
    void commentDtoResponseDeserializationTest() throws IOException {
        assertThat(jacksonTester.parse(json))
                .usingRecursiveComparison()
                .isEqualTo(commentDtoResponse);
    }
}