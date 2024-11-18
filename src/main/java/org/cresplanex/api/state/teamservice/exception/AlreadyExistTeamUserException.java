package org.cresplanex.api.state.teamservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AlreadyExistTeamUserException extends RuntimeException {

    private final List<String> userIds;

    public AlreadyExistTeamUserException(List<String> userIds) {
        super("Already exist team user with userIds: " + userIds.stream().reduce((a, b) -> a + ", " + b).orElse(""));
        this.userIds = userIds;
    }
}
