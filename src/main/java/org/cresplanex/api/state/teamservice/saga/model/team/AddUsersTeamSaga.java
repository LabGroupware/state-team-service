package org.cresplanex.api.state.teamservice.saga.model.team;

import org.cresplanex.api.state.common.constants.TeamServiceApplicationCode;
import org.cresplanex.api.state.common.event.model.team.TeamAddedUsers;
import org.cresplanex.api.state.common.event.model.team.TeamDomainEvent;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.data.team.AddUsersTeamResultData;
import org.cresplanex.api.state.common.saga.local.team.NotAllowedOnDefaultTeamException;
import org.cresplanex.api.state.common.saga.local.team.NotFoundTeamException;
import org.cresplanex.api.state.common.saga.model.SagaModel;
import org.cresplanex.api.state.common.saga.reply.organization.OrganizationAndOrganizationUserExistValidateReply;
import org.cresplanex.api.state.common.saga.reply.team.AddUsersTeamReply;
import org.cresplanex.api.state.common.saga.type.TeamSagaType;
import org.cresplanex.api.state.teamservice.constants.ActionOnTeam;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.event.publisher.TeamDomainEventPublisher;
import org.cresplanex.api.state.teamservice.saga.proxy.TeamServiceProxy;
import org.cresplanex.api.state.teamservice.saga.proxy.OrganizationServiceProxy;
import org.cresplanex.api.state.teamservice.saga.state.team.AddUsersTeamSagaState;
import org.cresplanex.api.state.teamservice.service.TeamLocalValidateService;
import org.cresplanex.core.saga.orchestration.SagaDefinition;
import org.springframework.stereotype.Component;

@Component
public class AddUsersTeamSaga extends SagaModel<
        TeamEntity,
        TeamDomainEvent,
        AddUsersTeamSaga.Action,
        AddUsersTeamSagaState> {

    private final SagaDefinition<AddUsersTeamSagaState> sagaDefinition;
    private final TeamDomainEventPublisher domainEventPublisher;
    private final TeamLocalValidateService teamLocalService;

    public AddUsersTeamSaga(
            TeamLocalValidateService teamLocalService,
            TeamServiceProxy teamService,
            OrganizationServiceProxy organizationService,
            TeamDomainEventPublisher domainEventPublisher
    ) {
        this.sagaDefinition = step()
                .invokeLocal(this::validateTeam)
                .onException(NotFoundTeamException.class, this::failureLocalExceptionPublish)
                .onException(NotAllowedOnDefaultTeamException.class, this::failureLocalExceptionPublish)
                .step()
                .invokeParticipant(
                        organizationService.organizationUserExistValidateCommand,
                        AddUsersTeamSagaState::makeOrganizationAndOrganizationUserExistValidateCommand
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
                        teamService.addUsersTeam,
                        AddUsersTeamSagaState::makeAddUsersTeamCommand
                )
                .onReply(
                        AddUsersTeamReply.Success.class,
                        AddUsersTeamReply.Success.TYPE,
                        this::handleAddUsersTeamReply
                )
                .onReply(
                        AddUsersTeamReply.Failure.class,
                        AddUsersTeamReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .withCompensation(
                        teamService.undoAddUsersTeam,
                        AddUsersTeamSagaState::makeUndoAddUsersTeamCommand
                )
                .build();
        this.teamLocalService = teamLocalService;
        this.domainEventPublisher = domainEventPublisher;
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
        return TeamAddedUsers.BeginJobDomainEvent.TYPE;
    }

    @Override
    protected String getProcessedEventType() {
        return TeamAddedUsers.ProcessedJobDomainEvent.TYPE;
    }

    @Override
    protected String getFailedEventType() {
        return TeamAddedUsers.FailedJobDomainEvent.TYPE;
    }

    @Override
    protected String getSuccessfullyEventType() {
        return TeamAddedUsers.SuccessJobDomainEvent.TYPE;
    }

    private void validateTeam(AddUsersTeamSagaState state)
            throws NotFoundTeamException {
        TeamEntity team = this.teamLocalService.validateTeam(
                state.getInitialData().getTeamId(),
                ActionOnTeam.ADD_USERS
        );
        state.setOrganizationId(team.getOrganizationId());

        this.localProcessedEventPublish(
                state, TeamServiceApplicationCode.SUCCESS, "Team validated"
        );
    }

    private void handleAddUsersTeamReply(
            AddUsersTeamSagaState state, AddUsersTeamReply.Success reply) {
        AddUsersTeamReply.Success.Data data = reply.getData();
        state.setAddedUsers(data.getAddedUsers());
        this.processedEventPublish(state, reply);
    }

    @Override
    public void onSagaCompletedSuccessfully(String sagaId, AddUsersTeamSagaState data) {
        AddUsersTeamResultData resultData = new AddUsersTeamResultData(data.getAddedUsers());
        successfullyEventPublish(data, resultData);
    }

    public enum Action {
        VALIDATE_TEAM,
        VALIDATE_ORGANIZATION_AND_ORGANIZATION_USER_EXIST,
        ADD_TEAM_USER
    }

    @Override
    public SagaDefinition<AddUsersTeamSagaState> getSagaDefinition() {
        return sagaDefinition;
    }

    @Override
    public String getSagaType() {
        return TeamSagaType.ADD_USERS_TO_TEAM;
    }

    @Override
    public String getSagaCommandSelfChannel() {
        return SagaCommandChannel.TEAM;
    }
}
