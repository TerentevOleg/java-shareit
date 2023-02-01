package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoRequestTest {
    @Autowired
    private JacksonTester<ItemRequestDtoRequest> jacksonTester;

    private final ItemRequestDtoRequest itemRequestDtoRequest = new ItemRequestDtoRequest("description");
    private final String json = "{\"description\": \"description\"}";

    @Test
    void itemRequestDtoRequestSerializationTest() throws IOException {
        assertThat(jacksonTester.write(itemRequestDtoRequest)).isEqualToJson(json, JSONCompareMode.STRICT);
    }

    @Test
    void itemRequestDtoRequestDeserializationTest() throws IOException {
        assertThat(jacksonTester.parse(json)).usingRecursiveComparison().isEqualTo(itemRequestDtoRequest);
    }
}