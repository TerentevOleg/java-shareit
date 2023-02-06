package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
@Slf4j
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getBookingById(long bookingId, long userId) {
        log.debug("BookingClient: get booking by bookingId=" + bookingId + " and userId=" + userId + ".");
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllBookingsByBooker(long userId, BookingState state, long from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", state.toString(),
                "from", from,
                "size", size
        );
        log.debug("BookingClient: get all bookings by bookerId=" + userId + ".");
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getAllBookingsByOwner(long userId, BookingState state, long from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", state.toString(),
                "from", from,
                "size", size
        );
        log.debug("BookingClient: get all bookings by bookerId=" + userId + ".");
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> add(BookingDtoRequest bookingDto, long userId) {
        log.debug("BookingClient: add booking by userId=" + userId + " and itemId=" + bookingDto.getItemId() + ".");
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> bookingStatus(long bookingId, long userId, boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }
}
