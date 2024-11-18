package org.cresplanex.api.state.teamservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class NotFoundTeamException extends RuntimeException {

    private final List<String> teamIds;

    public NotFoundTeamException(List<String> teamIds) {
        super("Not found team with teamIds: " + teamIds.stream().reduce((a, b) -> a + ", " + b).orElse(""));
        this.teamIds = teamIds;
    }
}
