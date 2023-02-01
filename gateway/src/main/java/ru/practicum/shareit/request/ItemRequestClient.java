package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> findById(long requestId, long userId) {
        return super.get("/" + requestId, userId);
    }

    public ResponseEntity<Object> findByRequester(long userId) {
        return super.get("", userId);
    }

    public ResponseEntity<Object> findByOtherUsers(long userId, long from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return super.get("/all?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> add(ItemRequestDtoRequest itemRequestDtoRequest, long userId) {
        return super.post("", userId, itemRequestDtoRequest);
    }
}
