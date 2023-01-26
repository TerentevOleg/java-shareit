package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.AuthenticationErrorException;
import ru.practicum.shareit.exception.CustomValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserPatchDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    private TestUserBuilder userBuilder;

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Accessors(chain = true, fluent = true)
    private static class TestUserBuilder {
        private Long id = 1L;
        private String name = "name";
        private String email = "user@mail.ru";

        public UserDto buildDto() {
            return new UserDto(id, name, email);
        }
    }

    @BeforeEach
    void setUp() {
        userBuilder = TestUserBuilder.defaultBuilder();
    }

    @Test
    void givenGetById_whenGetById_thenStatusOkAndJsonBody() throws Exception {
        UserDto userDto = userBuilder.buildDto();
        long userId = userDto.getId();
        String expectedJson = objectMapper.writeValueAsString(userDto);

        when(userService.getById(userDto.getId()))
                .thenReturn(userDto);

        mvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(userService, times(1)).getById(anyLong());
    }

    @Test
    void givenGetAll_whenGetAll_thenStatusOkAndJsonBody() throws Exception {
        List<UserDto> expectedDto = List.of(userBuilder.buildDto());

        when(userService.getAll())
                .thenReturn(expectedDto);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedDto)));

        verify(userService, times(1)).getAll();
    }

    @Test
    void givenAddValidDto_whenAddDto_thenStatusOkAndJsonBody() throws Exception {
        UserDto expectedDto = userBuilder.buildDto();
        userBuilder.id(null);
        UserDto requestDto = userBuilder.buildDto();

        when(userService.add(requestDto))
                .thenReturn(expectedDto);

        mvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedDto)));

        verify(userService, times(1)).add(any());
    }

    @Test
    void givenAddDtoWithBlankName_whenAddDto_thenStatusBadRequest() throws Exception {
        mvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userBuilder.name("").buildDto()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).add(any());
    }

    @Test
    void givenAddDtoWithBlankEmail_whenAddDto_thenStatusBadRequest() throws Exception {
        mvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userBuilder.email("").buildDto()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).add(any());
    }

    @Test
    void givenAddDtoWithIncorrectEmail_whenAddDto_thenStatusBadRequest() throws Exception {
        mvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userBuilder.email("mail").buildDto()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).add(any());
    }

    @Test
    void givenPatchWithValidName_whenPatch_thenStatusOkAndJsonBody() throws Exception {
        UserDto dto = userBuilder.buildDto();
        long userId = dto.getId();
        UserPatchDto patchDto = new UserPatchDto(dto.getName(), null);

        when(userService.patch(userId, patchDto))
                .thenReturn(dto);

        mvc.perform(patch("/users/{id}", userId)
                        .content(objectMapper.writeValueAsString(patchDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(userService, times(1)).patch(anyLong(), any());
    }

    @Test
    void givenPatchWithBlankName_whenPatch_thenStatusBadRequest() throws Exception {
        mvc.perform(patch("/users/{id}", 1)
                        .content(objectMapper.writeValueAsString(new UserPatchDto("",  null)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).patch(anyLong(), any());
    }

    @Test
    void givenPatchWithValidEmail_whenPatch_thenStatusOkAndJsonBody() throws Exception {
        UserDto dto = userBuilder.buildDto();
        long userId = dto.getId();
        UserPatchDto patchDto = new UserPatchDto(null, dto.getEmail());

        when(userService.patch(userId, patchDto))
                .thenReturn(dto);

        mvc.perform(patch("/users/{id}", userId)
                        .content(objectMapper.writeValueAsString(patchDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(userService, times(1)).patch(anyLong(), any());
    }

    @Test
    void givenPatchWithBlankEmail_whenPatch_thenStatusBadRequest() throws Exception {
        mvc.perform(patch("/users/{id}", 1)
                        .content(objectMapper.writeValueAsString(new UserPatchDto(null,  "")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).patch(anyLong(), any());
    }

    @Test
    void givenPatchWitIncorrectEmail_whenPatch_ThenStatusBadRequest() throws Exception {
        mvc.perform(patch("/users/{id}", 1)
                        .content(objectMapper.writeValueAsString(new UserPatchDto(null,  "mail")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).patch(anyLong(), any());
    }

    @Test
    void givenDelete_whenDelete_thenCallServiceMethodAndStatusOk() throws Exception {
        long userId = 1;
        mvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(userId);
    }

    @Test
    void handleNotFoundException() throws Exception {
        when(userService.getAll())
                .thenThrow(new NotFoundException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleCustomValidationException() throws Exception {
        when(userService.getAll())
                .thenThrow(new CustomValidationException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleDataIntegrityViolationException() throws Exception {
        when(userService.getAll())
                .thenThrow(new DataIntegrityViolationException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isConflict());
    }

    @Test
    void handleAuthenticationErrorException() throws Exception {
        when(userService.getAll())
                .thenThrow(new AuthenticationErrorException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void handleConstraintViolationException() throws Exception {
        when(userService.getAll())
                .thenThrow(new ConstraintViolationException(Set.of()));

        mvc.perform(get("/users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleUnexpectedException() throws Exception {
        when(userService.getAll())
                .thenThrow(new RuntimeException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isInternalServerError());
    }
}