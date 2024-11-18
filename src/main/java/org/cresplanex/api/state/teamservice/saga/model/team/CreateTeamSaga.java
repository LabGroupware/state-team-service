package org.cresplanex.api.state.teamservice.saga.model.team;

import org.cresplanex.api.state.common.constants.TeamServiceApplicationCode;
import org.cresplanex.api.state.common.dto.team.TeamWithUsersDto;
import org.cresplanex.api.state.common.event.model.team.TeamCreated;
import org.cresplanex.api.state.common.event.model.team.TeamDomainEvent;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.data.team.CreateTeamResultData;
import org.cresplanex.api.state.common.saga.local.team.AlreadyExistTeamNameInOrganizationException;
import org.cresplanex.api.state.common.saga.local.team.ReservedTeamNameException;
import org.cresplanex.api.state.common.saga.model.SagaModel;
import org.cresplanex.api.state.common.saga.reply.organization.OrganizationAndOrganizationUserExistValidateReply;
import org.cresplanex.api.state.common.saga.reply.team.CreateTeamAndAddInitialTeamUserReply;
import org.cresplanex.api.state.common.saga.type.TeamSagaType;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.event.publisher.TeamDomainEventPublisher;
import org.cresplanex.api.state.teamservice.saga.proxy.OrganizationServiceProxy;
import org.cresplanex.api.state.teamservice.saga.proxy.TeamServiceProxy;
import org.cresplanex.api.state.teamservice.saga.state.team.CreateTeamSagaState;
import org.cresplanex.api.state.teamservice.service.TeamService;
import org.cresplanex.core.saga.orchestration.SagaDefinition;
import org.springframework.stereotype.Component;

@Component
public class CreateTeamSaga extends SagaModel<
        TeamEntity,
        TeamDomainEvent,
        CreateTeamSaga.Action,
        CreateTeamSagaState> {

    private final SagaDefinition<CreateTeamSagaState> sagaDefinition;
    private final TeamDomainEventPublisher domainEventPublisher;
    private final TeamService teamLocalService;

    public CreateTeamSaga(
            TeamService teamLocalService,
            TeamServiceProxy teamService,
            OrganizationServiceProxy organizationService,
            TeamDomainEventPublisher domainEventPublisher
    ) {
        this.sagaDefinition = step()
                .invokeLocal(this::validateTeam)
                .onException(ReservedTeamNameException.class, this::failureLocalExceptionPublish)
                .onException(AlreadyExistTeamNameInOrganizationException.class, this::failureLocalExceptionPublish)
                .step()
                .invokeParticipant(
                        organizationService.organizationUserExistValidateCommand,
                        CreateTeamSagaState::makeOrganizationAndOrganizationUserExistValidateCommand
                )
                .onReply(
                        OrganizationAndOrganizationUserExistValidateReply.Success.class,
                        OrganizationAndOrganizationUserExistValidateReply.Success.TYPE,
                        this::processedEventPublish
                )
                .onReply(
                        OrganizationAndOrganizationUserExistValidateReply.Failure.class,
                        OrganizationAndOrganizationUserExistValidateReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .step()
                .invokeParticipant(
                        teamService.createTeamAndAddInitialTeamUser,
                        CreateTeamSagaState::makeCreateTeamAndAddInitialTeamUserCommand
                )
                .onReply(
                        CreateTeamAndAddInitialTeamUserReply.Success.class,
                        CreateTeamAndAddInitialTeamUserReply.Success.TYPE,
                        this::handleCreateTeamAndAddInitialTeamUserReply
                )
                .onReply(
                        CreateTeamAndAddInitialTeamUserReply.Failure.class,
                        CreateTeamAndAddInitialTeamUserReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .withCompensation(
                        teamService.undoCreateTeamAndAddInitialTeamUser,
                        CreateTeamSagaState::makeUndoCreateTeamAndAddInitialTeamUserCommand
                )
                .build();
        this.domainEventPublisher = domainEventPublisher;
        this.teamLocalService = teamLocalService;
    }

    @Override
    protected AggregateDomainEventPublisher<TeamEntity, TeamDomainEvent>
    getDomainEventPublisher() {
        return domainEventPublisher;
    }

    @Override
    protected Action[] getActions() {
        return Action.values();
    }

    @Override
    protected String getBeginEventType() {
        return TeamCreated.BeginJobDomainEvent.TYPE;
    }

    @Override
    protected String getProcessedEventType() {
        return TeamCreated.ProcessedJobDomainEvent.TYPE;
    }

    @Override
    protected String getFailedEventType() {
        return TeamCreated.FailedJobDomainEvent.TYPE;
    }

    @Override
    protected String getSuccessfullyEventType() {
        return TeamCreated.SuccessJobDomainEvent.TYPE;
    }

    private void validateTeam(CreateTeamSagaState state)
    throws AlreadyExistTeamNameInOrganizationException, ReservedTeamNameException {
        this.teamLocalService.validateCreatedTeam(
                state.getInitialData().getOrganizationId(),
                state.getInitialData().getName(),
                state.getInitialData().getDescription()
        );

        this.localProcessedEventPublish(
                state, TeamServiceApplicationCode.SUCCESS, "Team validated"
        );
    }

    private void handleCreateTeamAndAddInitialTeamUserReply(
            CreateTeamSagaState state,
            CreateTeamAndAddInitialTeamUserReply.Success reply
    ) {
        CreateTeamAndAddInitialTeamUserReply.Success.Data data = reply.getData();
        TeamWithUsersDto teamWithUsersDto =
                new TeamWithUsersDto(data.getTeam(), data.getUsers());
        state.setTeamWithUsersDto(teamWithUsersDto);
        this.processedEventPublish(state, reply);
    }

    @Override
    public void onSagaCompletedSuccessfully(String sagaId, CreateTeamSagaState data) {
        CreateTeamResultData resultData = new CreateTeamResultData(
                data.getTeamWithUsersDto()
        );
        successfullyEventPublish(data, resultData);
    }

    public enum Action {
        VALIDATE_TEAM,
        VALIDATE_ORGANIZATION_AND_ORGANIZATION_USER_EXIST,
        CREATE_TEAM_AND_ADD_INITIAL_TEAM_USER
    }

    @Override
    public SagaDefinition<CreateTeamSagaState> getSagaDefinition() {
        return sagaDefinition;
    }

    @Override
    public String getSagaType() {
        return TeamSagaType.CREATE_TEAM;
    }

    @Override
    public String getSagaCommandSelfChannel() {
        return SagaCommandChannel.TEAM;
    }
}
