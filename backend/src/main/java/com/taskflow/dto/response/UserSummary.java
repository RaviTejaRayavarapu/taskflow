package com.taskflow.dto.response;

import com.taskflow.model.User;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class UserSummary {
    private UUID   id;
    private String name;
    private String email;

    public static UserSummary from(User u) {
        return UserSummary.builder().id(u.getId()).name(u.getName()).email(u.getEmail()).build();
    }
}
