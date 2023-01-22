package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoResponseTest {
    @Autowired
    JacksonTester<ItemDtoResponse> jacksonTester;

    private final ItemDtoResponse itemDtoResponse = new ItemDtoResponse(
            1L, "name", "description", true, 2L,
            new BookingDtoShort(3L,
                    LocalDateTime.of(1993, 12, 3, 10, 5),
                    LocalDateTime.of(1994, 1, 3, 12, 10),
                    4L, BookingStatus.APPROVED),
            new BookingDtoShort(5L,
                    LocalDateTime.of(1994, 2, 3, 15, 20),
                    LocalDateTime.of(1994, 3, 4, 12, 10),
                    6L, BookingStatus.WAITING),
            List.of(
                    new CommentDtoResponse(11L, "text1", "Author1",
                            LocalDateTime.of(1995, 4, 5, 13, 15)),
                    new CommentDtoResponse(12L, "text2", "Author2",
                            LocalDateTime.of(1996, 4, 5, 13, 15)),
                    new CommentDtoResponse(13L, "text3", "Author3",
                            LocalDateTime.of(1997, 4,5, 13, 15))
            )
    );

    private final String json = "{\n" +
            "  \"id\": 1,\n" +
            "  \"name\": \"name\",\n" +
            "  \"description\": \"description\",\n" +
            "  \"available\": true,\n" +
            "  \"requestId\": 2,\n" +
            "  \"lastBooking\": {\n" +
            "    \"id\": 3,\n" +
            "    \"start\": \"1993-12-03T10:05:00\",\n" +
            "    \"end\": \"1994-01-03T12:10:00\",\n" +
            "    \"bookerId\": 4,\n" +
            "    \"status\": \"APPROVED\"\n" +
            "  },\n" +
            "  \"nextBooking\": {\n" +
            "    \"id\": 5,\n" +
            "    \"start\": \"1994-02-03T15:20:00\",\n" +
            "    \"end\": \"1994-03-04T12:10:00\",\n" +
            "    \"bookerId\": 6,\n" +
            "    \"status\": \"WAITING\"\n" +
            "  },\n" +
            "  \"comments\": [\n" +
            "    {\n" +
            "      \"id\": 11,\n" +
            "      \"text\": \"text1\",\n" +
            "      \"authorName\": \"Author1\",\n" +
            "      \"created\": \"1995-04-05T13:15:00\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 12,\n" +
            "      \"text\": \"text2\",\n" +
            "      \"authorName\": \"Author2\",\n" +
            "      \"created\": \"1996-04-05T13:15:00\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 13,\n" +
            "      \"text\": \"text3\",\n" +
            "      \"authorName\": \"Author3\",\n" +
            "      \"created\": \"1997-04-05T13:15:00\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Test
    void itemDtoResponseSerializationTest() throws IOException {
        assertThat(jacksonTester.write(itemDtoResponse))
                .isEqualToJson(json, JSONCompareMode.STRICT);
    }

    @Test
    void itemDtoResponseDeserializationTest() throws IOException {
        assertThat(jacksonTester.parse(json))
                .usingRecursiveComparison()
                .isEqualTo(itemDtoResponse);
    }
}