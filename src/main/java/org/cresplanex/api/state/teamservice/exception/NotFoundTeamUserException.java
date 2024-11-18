package org.cresplanex.api.state.teamservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class NotFoundTeamUserException extends RuntimeException {

    private final String teamId;
    private final List<String> userIds;

    public NotFoundTeamUserException(String teamId, List<String> userIds) {
        super("Not found team user with userIds: " + userIds.stream().reduce((a, b) -> a + ", " + b).orElse(""));
        this.teamId = teamId;
        this.userIds = userIds;
    }
}
