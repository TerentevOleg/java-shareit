package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemBuilderTestUtil;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserBuildersTestUtil;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    void search() {
        User user1 = UserBuildersTestUtil.defaultBuilder().userBuild();
        testEntityManager.persistAndGetId(user1);

        ItemBuilderTestUtil itemBuilder = ItemBuilderTestUtil.defaultBuilder().owner(user1);
        Item item1 = itemBuilder.name("name").description("description").itemBuilder();
        Item item2 = itemBuilder.name("HereIsTeXtToFind").description("description").itemBuilder();
        Item item3 = itemBuilder.name("name").description("Here id TeXt to find").itemBuilder();
        testEntityManager.persistAndGetId(item1);
        testEntityManager.persistAndGetId(item2);
        testEntityManager.persistAndGetId(item3);

        Set<Long> idsFromQuery = itemRepository.searchByNameOrDescription("text", Pageable.unpaged())
                .stream()
                .map(Item::getId)
                .collect(Collectors.toSet());

        assertEquals(Set.of(item2.getId(), item3.getId()), idsFromQuery);
    }
}