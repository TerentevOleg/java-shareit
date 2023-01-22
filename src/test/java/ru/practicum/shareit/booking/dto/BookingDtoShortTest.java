package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
class BookingDtoShortTest {
    @Autowired
    JacksonTester<BookingDtoShort> jacksonTester;

    private final BookingDtoShort bookingDtoShort = new BookingDtoShort(1L,
            LocalDateTime.of(1993, 12, 3, 10, 5),
            LocalDateTime.of(1994, 1, 3, 12, 10),
            2L, BookingStatus.WAITING);

    private final String json = "{\n" +
            "  \"id\": 1,\n" +
            "  \"start\": \"1993-12-03T10:05:00\",\n" +
            "  \"end\": \"1994-01-03T12:10:00\",\n" +
            "  \"bookerId\": 2,\n" +
            "  \"status\": \"WAITING\"\n" +
            "}";

    @Test
    void bookingDtoShortSerializationTest() throws IOException {
        assertThat(jacksonTester.write(bookingDtoShort))
                .isEqualToJson(json, JSONCompareMode.STRICT);
    }

    @Test
    void bookingDtoShortDeserializationTest() throws IOException {
        assertThat(jacksonTester.parse(json))
                .usingRecursiveComparison()
                .isEqualTo(bookingDtoShort);
    }
}