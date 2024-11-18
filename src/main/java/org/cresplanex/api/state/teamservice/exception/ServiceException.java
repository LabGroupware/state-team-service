package org.cresplanex.api.state.teamservice.exception;

import build.buf.gen.team.v1.TeamServiceErrorCode;

public abstract class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    abstract public TeamServiceErrorCode getServiceErrorCode();
    abstract public String getErrorCaption();
}
