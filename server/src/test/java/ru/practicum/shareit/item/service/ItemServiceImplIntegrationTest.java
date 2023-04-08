package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserBuildersTestUtil;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemServiceImplIntegrationTest {

    private final ItemService itemService;

    private final EntityManager entityManager;

    @Test
    void add() {
        List<User> users = List.of(
                UserBuildersTestUtil.all(null, "name1", "user1@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name2", "user2@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name3", "user3@mail.ru").userBuild()
        );
        users.forEach(entityManager::persist);

        ItemDto itemDto = new ItemDto(null, "itemName", "itemDescription",
                true, null);

        itemService.add(itemDto, users.get(0).getId());

        List<Item> items = entityManager.createQuery("SELECT i FROM Item i", Item.class)
                .getResultList();

        assertThat(items.size()).isEqualTo(1);
        Item item = items.get(0);
        assertThat(item.getId()).isNotNull();
        assertThat(item.getName()).isEqualTo("itemName");
        assertThat(item.getDescription()).isEqualTo("itemDescription");
        assertThat(item.getAvailable()).isEqualTo(true);
        assertThat(item.getRequest()).isEqualTo(null);
    }
}
