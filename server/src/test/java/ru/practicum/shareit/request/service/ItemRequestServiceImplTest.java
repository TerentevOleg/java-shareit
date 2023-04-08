package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.booking.mapper.BookingShortMapperImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapperImpl;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.mapper.ItemRequestMapperImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Spy
    private static ItemRequestMapper itemRequestMapper = new ItemRequestMapperImpl();

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Accessors(chain = true, fluent = true)
    private static class TestRequestBuilder {
        private Long id = 1L;
        private String description = "description1";
        private LocalDateTime created = LocalDateTime.of(1993, 12, 3, 10, 1);
        private Long requesterId = 11L;
        private Long itemId = 101L;
        private Long itemOwnerId = 111L;

        public ItemRequest itemRequestBuild() {
            ItemRequest request = new ItemRequest();
            request.setId(id);
            request.setDescription(description);
            request.setCreated(created);
            User requester = new User();
            requester.setId(requesterId);
            request.setRequester(requester);
            return request;
        }

        public List<Item> buildItems(ItemRequest request) {
            Item item1 = new Item();
            item1.setId(itemId);
            User owner1 = new User();
            owner1.setId(itemOwnerId);
            item1.setOwner(owner1);
            item1.setRequest(request);
            return List.of(item1);
        }

        public ItemRequestDtoResponse buildDtoResponse() {
            return new ItemRequestDtoResponse(id, description, created);
        }

        public ItemRequestDto buildRequestDto() {
            return new ItemRequestDto(id, description, created,
                    List.of(new ItemDto(itemId, null, null, null, id)));
        }
    }

    @BeforeAll
    static void beforeAll() {
        ItemMapper itemMapper = new ItemMapperImpl();
        ReflectionTestUtils.setField(itemRequestMapper, "itemMapper", itemMapper);
        ReflectionTestUtils.setField(itemMapper, "bookingShortMapper", new BookingShortMapperImpl());
        ReflectionTestUtils.setField(itemMapper, "commentMapper", new CommentMapperImpl());
    }

    @Test
    void givenAddWithCorrectDto_whenAdd_thenSaveAndReturnDto() {
        TestRequestBuilder requestBuilder = TestRequestBuilder.defaultBuilder();
        ItemRequest savedItem = requestBuilder.itemRequestBuild();
        ItemRequestDtoResponse dto = requestBuilder.buildDtoResponse();
        requestBuilder.id(null).created(null);
        ItemRequest requestItem = requestBuilder.itemRequestBuild();
        ItemRequestDtoRequest inputDto = new ItemRequestDtoRequest(requestItem.getDescription());
        User requester = requestItem.getRequester();
        long userId = requester.getId();

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(requester));
        Mockito.when(itemRequestRepository.save(Mockito.any()))
                .thenReturn(savedItem);

        LocalDateTime minCreated = LocalDateTime.now();
        assertEquals(dto, itemRequestService.add(inputDto, userId));
        LocalDateTime maxCreated = LocalDateTime.now();

        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .save(Mockito.argThat(
                        itemRequest -> Objects.nonNull(itemRequest) &&
                                Objects.equals(itemRequest.getId(), requestItem.getId()) &&
                                Objects.equals(itemRequest.getDescription(), requestItem.getDescription()) &&
                                !itemRequest.getCreated().isBefore(minCreated) &&
                                !itemRequest.getCreated().isAfter(maxCreated) &&
                                Objects.nonNull(itemRequest.getRequester()) &&
                                Objects.equals(itemRequest.getRequester().getId(), requestItem.getRequester().getId())
                ));
    }

    @Test
    void givenAddWithIncorrectRequesterId_whenAdd_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.add(new ItemRequestDtoRequest("description"), 1L));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void givenFindByIdCorrect_whenFind_thenReturnDto() {
        TestRequestBuilder requestBuilder = TestRequestBuilder.defaultBuilder();
        ItemRequest itemRequest1 = requestBuilder.itemRequestBuild();
        ItemRequestDto dto = requestBuilder.buildRequestDto();
        long userId = itemRequest1.getRequester().getId() + 1;
        User user = new User();
        user.setId(userId);

        Mockito.lenient().when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.lenient().when(itemRequestRepository.findById(itemRequest1.getId()))
                .thenReturn(Optional.of(itemRequest1));
        Mockito.lenient().when(itemRepository.findAllByRequest(Mockito.argThat(itemRequest ->
                        Objects.nonNull(itemRequest) &&
                                Objects.nonNull(itemRequest.getId()) &
                                        Objects.equals(itemRequest.getDescription(), itemRequest1.getDescription()) &&
                                Objects.equals(itemRequest.getCreated(), itemRequest1.getCreated()))))
                .thenReturn(requestBuilder.buildItems(itemRequest1));

        assertEquals(dto, itemRequestService.findById(itemRequest1.getId(), userId));
    }

    @Test
    void givenFindByIdWithIncorrectUserId_whenFind_thenThrowNotFoundException() {
        TestRequestBuilder requestBuilder = TestRequestBuilder.defaultBuilder();
        ItemRequest itemRequest = requestBuilder.itemRequestBuild();
        long userId = itemRequest.getRequester().getId() + 1;
        User user = new User();
        user.setId(userId);

        Mockito.lenient().when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
        Mockito.lenient().when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));

        assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(itemRequest.getId(), userId));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(userId);
    }

    @Test
    void givenFindByIdWithIncorrectRequestId_whenFind_thenThrowNotFoundException() {
        TestRequestBuilder requestBuilder = TestRequestBuilder.defaultBuilder();
        ItemRequest itemRequest = requestBuilder.itemRequestBuild();
        long userId = itemRequest.getRequester().getId() + 1;
        User user = new User();
        user.setId(userId);
        long id = itemRequest.getId() + 1;

        Mockito.lenient().when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.lenient().when(itemRequestRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(id, userId));

        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findById(id);
    }

    @Test
    void givenFindByRequesterCorrect_whenFind_thenReturnListOfDto() {
        TestRequestBuilder requestBuilder = TestRequestBuilder.defaultBuilder();
        ItemRequest itemRequest = requestBuilder.itemRequestBuild();
        User requester = itemRequest.getRequester();
        long requesterId = requester.getId();
        ItemRequestDto dto = requestBuilder.buildRequestDto();

        Mockito.when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        Mockito.when(itemRequestRepository.findAllByRequester(Mockito.any(), Mockito.any()))
                .thenReturn(List.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIn(Mockito.argThat(itemRequestList ->
                        Objects.nonNull(itemRequestList) &&
                                itemRequestList.size() == 1 &&
                                Objects.nonNull(itemRequestList.get(0).getId()) &&
                                Objects.equals(itemRequestList.get(0).getDescription(), itemRequest.getDescription()) &&
                                Objects.equals(itemRequestList.get(0).getCreated(), itemRequest.getCreated()))))
                .thenReturn(requestBuilder.buildItems(itemRequest));

        assertEquals(List.of(dto), itemRequestService.findByRequester(requesterId));

        Mockito.verify(itemRequestRepository, Mockito.atLeastOnce())
                .findAllByRequester(
                        Mockito.argThat(user -> Objects.nonNull(user) &&
                                Objects.equals(user.getId(), requester.getId())),
                        Mockito.eq(Sort.by(Sort.Direction.DESC, "created"))
                );
    }

    @Test
    void givenFindByRequesterWithIncorrectRequesterId_whenFind_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(2L, 1L));
    }

    @Test
    void givenFindByRequesterWithNoRequests_whenFind_thenReturnEmptyList() {
        TestRequestBuilder requestBuilder = TestRequestBuilder.defaultBuilder();
        ItemRequest itemRequest = requestBuilder.itemRequestBuild();

        Mockito.when(userRepository.findById(itemRequest.getRequester().getId()))
                .thenReturn(Optional.of(itemRequest.getRequester()));
        Mockito.when(itemRequestRepository.findAllByRequester(Mockito.any(), Mockito.any()))
                .thenReturn(List.of());

        assertEquals(Collections.emptyList(), itemRequestService.findByRequester(itemRequest.getRequester().getId()));
    }

    @Test
    void givenFindByOtherUsersCorrectWithPagination_whenFind_thenReturnListOfDto() {
        TestRequestBuilder requestBuilder = TestRequestBuilder.defaultBuilder();
        ItemRequest itemRequest = requestBuilder.itemRequestBuild();
        long userId = itemRequest.getRequester().getId() + 1;
        User user = new User();
        user.setId(userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "created");

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findAllByRequesterIsNot(
                        Mockito.argThat(requester -> Objects.nonNull(requester) &&
                                Objects.equals(requester.getId(), userId)),
                        Mockito.eq(PageRequest.of(0, 1, sort))))
                .thenReturn(List.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIn(Mockito.argThat(itemRequestList ->
                        Objects.nonNull(itemRequestList) &&
                                itemRequestList.size() == 1 &&
                                Objects.nonNull(itemRequestList.get(0).getId()) &&
                                Objects.equals(itemRequestList.get(0).getDescription(), itemRequest.getDescription()) &&
                                Objects.equals(itemRequestList.get(0).getCreated(), itemRequest.getCreated()))))
                .thenReturn(requestBuilder.buildItems(itemRequest));

        assertEquals(List.of(requestBuilder.buildRequestDto()),
                itemRequestService.findByOtherUsers(userId, 0, 1));
    }

    @Test
    void givenFindByOtherUsersWithIncorrectUserId_whenFind_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.findByOtherUsers(1L, 0, 1));
    }

}