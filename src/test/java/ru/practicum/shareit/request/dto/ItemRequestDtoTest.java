package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoTest {
    @Autowired
    private JacksonTester<ItemRequestDto> jacksonTester;

    private final String json = "{\n" +
            "  \"id\": 1,\n" +
            "  \"description\": \"description\",\n" +
            "  \"created\": \"1993-12-03T10:05:00\",\n" +
            "  \"items\": [\n" +
            "    {\n" +
            "      \"id\": 2,\n" +
            "      \"name\": \"item1\",\n" +
            "      \"description\": \"description1\",\n" +
            "      \"available\": true,\n" +
            "      \"requestId\": 3\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 4,\n" +
            "      \"name\": \"item2\",\n" +
            "      \"description\": \"description2\",\n" +
            "      \"available\": false,\n" +
            "      \"requestId\": 5\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private final ItemRequestDto itemRequestDtoOutExtended = new ItemRequestDto(1L,
            "description",
            LocalDateTime.of(1993, 12, 3, 10, 5),
            List.of(new ItemDto(2L, "item1", "description1", true, 3L),
                    new ItemDto(4L, "item2", "description2", false, 5L)));

    @Test
    void itemRequestDtoSerializationTest() throws IOException {
        assertThat(jacksonTester.write(itemRequestDtoOutExtended))
                .isEqualToJson(json, JSONCompareMode.STRICT);
    }

    @Test
    void itemRequestDtoDeserializationTest() throws IOException {
        assertThat(jacksonTester.parse(json))
                .usingRecursiveComparison()
                .isEqualTo(itemRequestDtoOutExtended);
    }
}