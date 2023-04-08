package ru.practicum.shareit.request.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(staticName = "defaultBuilder")
@AllArgsConstructor(staticName = "all")
@Setter
@Accessors(chain = true, fluent = true)
public class ItemRequestBuilderTestUtil {
    private Long id;
    private String description = "description";
    private User requester;
    private LocalDateTime created;

    public ItemRequest itemRequestBuilder() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(id);
        itemRequest.setDescription(description);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(created);
        return itemRequest;
    }
}
