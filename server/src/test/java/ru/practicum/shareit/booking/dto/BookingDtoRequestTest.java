package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoRequestTest {
    @Autowired
    private JacksonTester<BookingDtoRequest> jacksonTester;

    private final String json = "{\n" +
            "  \"itemId\": 1,\n" +
            "  \"start\": \"1993-12-03T10:05:00\",\n" +
            "  \"end\": \"1994-01-03T12:10:00\"\n" +
            "}";

    private final BookingDtoRequest bookingDtoRequest = new BookingDtoRequest(
            LocalDateTime.of(1993, 12, 3, 10, 5),
            LocalDateTime.of(1994, 1, 3, 12, 10),
            1L
    );

    @Test
    void bookingDtoRequestSerializationTest() throws IOException {
        assertThat(jacksonTester.write(bookingDtoRequest)).isEqualToJson(json, JSONCompareMode.STRICT);
    }

    @Test
    void bookingDtoRequestDeserializationTest() throws IOException {
        assertThat(jacksonTester.parse(json))
                .usingRecursiveComparison()
                .isEqualTo(bookingDtoRequest);
    }
}