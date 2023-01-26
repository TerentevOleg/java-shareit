package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor(staticName = "defaultBuilder")
@AllArgsConstructor(staticName = "all")
@Setter
@Accessors(chain = true, fluent = true)
public class UserBuildersTestUtil {
    private Long id;
    private String name = "name";
    private String email = "user@mail.ru";

    public User userBuild() {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
