package org.cresplanex.api.state.teamservice.exception;

import build.buf.gen.team.v1.*;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {

     @GrpcExceptionHandler(TeamNotFoundException.class)
     public Status handleTeamNotFoundException(TeamNotFoundException e) {
        TeamServiceTeamNotFoundError.Builder descriptionBuilder =
                TeamServiceTeamNotFoundError.newBuilder()
                .setMeta(buildErrorMeta(e));

        switch (e.getFindType()) {
            case BY_ID:
                descriptionBuilder
                        .setFindFieldType(TeamUniqueFieldType.TEAM_UNIQUE_FIELD_TYPE_TEAM_ID)
                        .setTeamId(e.getAggregateId());
                break;
        }

         return Status.NOT_FOUND
                 .withDescription(descriptionBuilder.build().toString())
                 .withCause(e);
     }

     private TeamServiceErrorMeta buildErrorMeta(ServiceException e) {
         return TeamServiceErrorMeta.newBuilder()
                 .setCode(e.getServiceErrorCode())
                 .setMessage(e.getErrorCaption())
                 .build();
     }

    @GrpcExceptionHandler
    public Status handleInternal(Throwable e) {
         TeamServiceInternalError.Builder descriptionBuilder =
                 TeamServiceInternalError.newBuilder()
                         .setMeta(TeamServiceErrorMeta.newBuilder()
                                 .setCode(TeamServiceErrorCode.TEAM_SERVICE_ERROR_CODE_INTERNAL)
                                 .setMessage(e.getMessage())
                                 .build());

         return Status.INTERNAL
                 .withDescription(descriptionBuilder.build().toString())
                 .withCause(e);
    }
}
