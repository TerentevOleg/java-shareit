package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoResponseTest {
    @Autowired
    JacksonTester<BookingDtoResponse> jacksonTester;

    private final String json = "{\n" +
            "  \"id\": 1,\n" +
            "  \"start\": \"1993-12-03T10:05:00\",\n" +
            "  \"end\": \"1994-01-03T12:10:00\",\n" +
            "  \"item\": {\n" +
            "    \"id\":2,\n" +
            "    \"name\": \"itemName\",\n" +
            "    \"description\": \"itemDescription\",\n" +
            "    \"available\": true,\n" +
            "    \"requestId\": 3\n" +
            "  },\n" +
            "  \"booker\": {\n" +
            "    \"id\": 4,\n" +
            "    \"name\": \"userName\",\n" +
            "    \"email\": \"user@mail.com\"\n" +
            "  },\n" +
            "  \"status\": \"WAITING\"\n" +
            "}";

    private final BookingDtoResponse bookingDtoResponse = new BookingDtoResponse(1L,
            LocalDateTime.of(1993, 12, 3, 10, 5),
            LocalDateTime.of(1994, 1, 3, 12, 10),
            new ItemDto(2L, "itemName", "itemDescription", true, 3L),
            new UserDto(4L, "userName", "user@mail.com"),
            BookingStatus.WAITING);

    @Test
    void bookingDtoResponseSerializationTest() throws IOException {
        assertThat(jacksonTester.write(bookingDtoResponse))
                .isEqualToJson(json, JSONCompareMode.STRICT);
    }

    @Test
    void bookingDtoResponseDeserializationTest() throws IOException {
        assertThat(jacksonTester.parse(json))
                .usingRecursiveComparison()
                .isEqualTo(bookingDtoResponse);
    }
}