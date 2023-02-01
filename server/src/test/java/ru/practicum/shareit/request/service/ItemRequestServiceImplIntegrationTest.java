package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemBuilderTestUtil;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestBuilderTestUtil;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserBuildersTestUtil;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestServiceImplIntegrationTest {

    private final ItemRequestService itemRequestService;

    private final EntityManager entityManager;

    @Test
    void findById() {
        List<User> users = List.of(
                UserBuildersTestUtil.all(null, "name1", "user1@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name2", "user2@mail.ru").userBuild(),
                UserBuildersTestUtil.all(null, "name3", "user3@mail.ru").userBuild()
        );

        ItemBuilderTestUtil itemBuilderTestUtil = ItemBuilderTestUtil.defaultBuilder();
        List<Item> items = List.of(
                itemBuilderTestUtil.name("itemName1").description("description1")
                        .owner(users.get(0)).itemBuilder(),
                itemBuilderTestUtil.name("itemName2").description("description2")
                        .owner(users.get(1)).itemBuilder(),
                itemBuilderTestUtil.name("itemName3").description("description3")
                        .owner(users.get(2)).itemBuilder()
        );

        List<ItemRequest> itemRequests = List.of(
                ItemRequestBuilderTestUtil.all(null, "descriptionItemRequest", users.get(1),
                        LocalDateTime.of(1993, 12, 3, 10, 5)).itemRequestBuilder()
        );

        users.forEach(entityManager::persist);
        items.forEach(entityManager::persist);
        itemRequests.forEach(entityManager::persist);

        Long id = itemRequests.get(0).getId();
        Long userId = itemRequests.get(0).getRequester().getId();
        ItemRequestDto itemRequestDto = itemRequestService.findById(id, userId);

        assertThat(itemRequestDto).isEqualTo(new ItemRequestDto(1L, "descriptionItemRequest",
                LocalDateTime.of(1993, 12, 3, 10, 5), Collections.emptyList()));
    }
}
