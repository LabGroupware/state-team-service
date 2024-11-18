package org.cresplanex.api.state.teamservice.saga.state.team;

import lombok.*;
import org.cresplanex.api.state.common.dto.team.UserOnTeamDto;
import org.cresplanex.api.state.common.saga.command.team.AddUsersTeamCommand;
import org.cresplanex.api.state.common.saga.state.SagaState;
import org.cresplanex.api.state.common.saga.validate.organization.OrganizationAndOrganizationUserExistValidateCommand;
import org.cresplanex.api.state.common.saga.validate.userprofile.UserExistValidateCommand;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.saga.model.team.AddUsersTeamSaga;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AddUsersTeamSagaState
        extends SagaState<AddUsersTeamSaga.Action, TeamEntity> {
    private InitialData initialData;
    private List<UserOnTeamDto> addedUsers = new ArrayList<>();
    private String operatorId;
    private String organizationId;

    @Override
    public String getId() {
        return initialData.teamId;
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
        private String teamId;
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
                this.organizationId,
                initialData.getUsers().stream()
                        .map(AddUsersTeamSagaState.InitialData.User::getUserId)
                        .toList());
    }

    public AddUsersTeamCommand.Exec makeAddUsersTeamCommand() {
        return new AddUsersTeamCommand.Exec(
                this.operatorId,
                initialData.getTeamId(),
                initialData.getUsers().stream()
                        .map(user -> new AddUsersTeamCommand.Exec.User(user.getUserId()))
                        .toList()
        );
    }

    public AddUsersTeamCommand.Undo makeUndoAddUsersTeamCommand() {
        return new AddUsersTeamCommand.Undo(
                addedUsers.stream()
                        .map(UserOnTeamDto::getUserTeamId)
                        .toList()
        );
    }
}
