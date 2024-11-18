package org.cresplanex.api.state.teamservice.exception;

import build.buf.gen.team.v1.TeamServiceErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeamNotFoundException extends ServiceException {

    private final FindType findType;
    private final String aggregateId;

    public TeamNotFoundException(FindType findType, String aggregateId) {
        this(findType, aggregateId, "Model not found: " + findType.name() + " with id " + aggregateId);
    }

    public TeamNotFoundException(FindType findType, String aggregateId, String message) {
        super(message);
        this.findType = findType;
        this.aggregateId = aggregateId;
    }

    public TeamNotFoundException(FindType findType, String aggregateId, String message, Throwable cause) {
        super(message, cause);
        this.findType = findType;
        this.aggregateId = aggregateId;
    }

    public enum FindType {
        BY_ID,
        BY_ORGANIZATION_ID_AND_IS_DEFAULT
    }

    @Override
    public TeamServiceErrorCode getServiceErrorCode() {
        return TeamServiceErrorCode.TEAM_SERVICE_ERROR_CODE_TEAM_NOT_FOUND;
    }

    @Override
    public String getErrorCaption() {
        return switch (findType) {
            case BY_ID -> "Team not found (ID = %s)".formatted(aggregateId);
            case BY_ORGANIZATION_ID_AND_IS_DEFAULT -> "Default team not found (Organization ID = %s)".formatted(aggregateId);
            default -> "Team not found";
        };
    }
}
