package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoRequestTest {
    @Autowired
    private JacksonTester<CommentDtoRequest> jacksonTester;

    private final CommentDtoRequest commentDtoRequest = new CommentDtoRequest("text");
    private final String json = "{\"text\": \"text\"}";

    @Test
    void commentDtoRequestSerializationTest() throws IOException {
        assertThat(jacksonTester.write(commentDtoRequest)).isEqualToJson(json, JSONCompareMode.STRICT);
    }

    @Test
    void commentDtoRequestDeserializationTest() throws IOException {
        assertThat(jacksonTester.parse(json)).usingRecursiveComparison().isEqualTo(commentDtoRequest);
    }
}