package org.cresplanex.api.state.teamservice.saga.state.team;

import lombok.*;
import org.cresplanex.api.state.common.dto.team.TeamWithUsersDto;
import org.cresplanex.api.state.common.dto.team.TeamDto;
import org.cresplanex.api.state.common.saga.command.team.CreateTeamAndAddInitialTeamUserCommand;
import org.cresplanex.api.state.common.saga.state.SagaState;
import org.cresplanex.api.state.common.saga.validate.organization.OrganizationAndOrganizationUserExistValidateCommand;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.saga.model.team.CreateTeamSaga;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class CreateTeamSagaState
        extends SagaState<CreateTeamSaga.Action, TeamEntity> {
    private InitialData initialData;
    private TeamWithUsersDto teamWithUsersDto = TeamWithUsersDto.empty();
    private TeamDto teamDto = TeamDto.empty();
    private String operatorId;

    @Override
    public String getId() {
        return teamWithUsersDto.getTeam().getTeamId();
    }

    @Override
    public Class<TeamEntity> getEntityClass() {
        return TeamEntity.class;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InitialData {
        private String organizationId;
        private String name;
        private String description;
        private List<User> users;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class User{
            private String userId;
        }
    }

    public OrganizationAndOrganizationUserExistValidateCommand makeOrganizationAndOrganizationUserExistValidateCommand() {
        return new OrganizationAndOrganizationUserExistValidateCommand(
                initialData.getOrganizationId(),
                initialData.getUsers().stream()
                .map(InitialData.User::getUserId)
                .toList());
    }

    public CreateTeamAndAddInitialTeamUserCommand.Exec makeCreateTeamAndAddInitialTeamUserCommand() {
        return new CreateTeamAndAddInitialTeamUserCommand.Exec(
                this.operatorId,
                initialData.getOrganizationId(),
                initialData.getName(),
                initialData.getDescription(),
                initialData.getUsers().stream()
                        .map(user -> new CreateTeamAndAddInitialTeamUserCommand.Exec.User(user.getUserId()))
                        .toList()
        );
    }

    public CreateTeamAndAddInitialTeamUserCommand.Undo makeUndoCreateTeamAndAddInitialTeamUserCommand() {
        return new CreateTeamAndAddInitialTeamUserCommand.Undo(teamWithUsersDto.getTeam().getTeamId());
    }
}
