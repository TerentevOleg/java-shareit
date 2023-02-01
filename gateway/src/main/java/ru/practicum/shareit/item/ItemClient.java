package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemPatchDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getById(long itemId, long userId) {
        return super.get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAll(long userId, long from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return super.get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> add(ItemDto itemDto, long userId) {
        return super.post("", userId, itemDto);
    }

    public ResponseEntity<Object> addComment(CommentDtoRequest dto, long itemId, long userId) {
        return super.post("/" + itemId + "/comment", userId, dto);
    }

    public ResponseEntity<Object> patch(long itemId, ItemPatchDto patchDto, long userId) {
        return super.patch("/" + itemId, userId, patchDto);
    }

    public ResponseEntity<Object> search(String text, long from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size,
                "text", text
        );
        return super.get("/search?from={from}&size={size}&text={text}", null, parameters);
    }
}
