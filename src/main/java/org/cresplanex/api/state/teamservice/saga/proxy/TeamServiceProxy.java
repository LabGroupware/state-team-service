package org.cresplanex.api.state.teamservice.saga.proxy;

import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.command.organization.AddUsersOrganizationCommand;
import org.cresplanex.api.state.common.saga.command.organization.CreateOrganizationAndAddInitialOrganizationUserCommand;
import org.cresplanex.api.state.common.saga.command.team.AddUsersTeamCommand;
import org.cresplanex.api.state.common.saga.command.team.CreateTeamAndAddInitialTeamUserCommand;
import org.cresplanex.core.saga.simpledsl.CommandEndpoint;
import org.cresplanex.core.saga.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class TeamServiceProxy {

    public final CommandEndpoint<CreateTeamAndAddInitialTeamUserCommand.Exec> createTeamAndAddInitialTeamUser
            = CommandEndpointBuilder
            .forCommand(CreateTeamAndAddInitialTeamUserCommand.Exec.class)
            .withChannel(SagaCommandChannel.TEAM)
            .withCommandType(CreateTeamAndAddInitialTeamUserCommand.Exec.TYPE)
            .build();

    public final CommandEndpoint<CreateTeamAndAddInitialTeamUserCommand.Undo> undoCreateTeamAndAddInitialTeamUser
            = CommandEndpointBuilder
            .forCommand(CreateTeamAndAddInitialTeamUserCommand.Undo.class)
            .withChannel(SagaCommandChannel.TEAM)
            .withCommandType(CreateTeamAndAddInitialTeamUserCommand.Undo.TYPE)
            .build();

    public final CommandEndpoint<AddUsersTeamCommand.Exec> addUsersTeam
            = CommandEndpointBuilder
            .forCommand(AddUsersTeamCommand.Exec.class)
            .withChannel(SagaCommandChannel.TEAM)
            .withCommandType(AddUsersTeamCommand.Exec.TYPE)
            .build();

    public final CommandEndpoint<AddUsersTeamCommand.Undo> undoAddUsersTeam
            = CommandEndpointBuilder
            .forCommand(AddUsersTeamCommand.Undo.class)
            .withChannel(SagaCommandChannel.TEAM)
            .withCommandType(AddUsersTeamCommand.Undo.TYPE)
            .build();
}
