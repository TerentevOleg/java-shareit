package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(staticName = "defaultBuilder")
@AllArgsConstructor(staticName = "all")
@Setter
@Accessors(chain = true, fluent = true)
public class ItemBuilderTestUtil {
    private Long id;
    private String name = "name";
    private String description = "description";
    private Boolean available = true;
    private User owner;
    private ItemRequest request;

    public Item itemBuilder() {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}
