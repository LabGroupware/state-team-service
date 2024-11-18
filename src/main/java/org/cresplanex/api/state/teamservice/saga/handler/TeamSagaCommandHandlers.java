package org.cresplanex.api.state.teamservice.saga.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.constants.TeamServiceApplicationCode;
import org.cresplanex.api.state.common.saga.LockTargetType;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.command.team.AddUsersDefaultTeamCommand;
import org.cresplanex.api.state.common.saga.command.team.AddUsersTeamCommand;
import org.cresplanex.api.state.common.saga.command.team.CreateDefaultTeamAndAddInitialDefaultTeamUserCommand;
import org.cresplanex.api.state.common.saga.command.team.CreateTeamAndAddInitialTeamUserCommand;
import org.cresplanex.api.state.common.saga.reply.team.*;
import org.cresplanex.api.state.common.saga.validate.team.TeamExistValidateCommand;
import org.cresplanex.api.state.teamservice.constants.ReservedTeamName;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.cresplanex.api.state.teamservice.exception.AlreadyExistTeamUserException;
import org.cresplanex.api.state.teamservice.exception.NotFoundTeamException;
import org.cresplanex.api.state.teamservice.mapper.dto.DtoMapper;
import org.cresplanex.api.state.teamservice.service.TeamService;
import org.cresplanex.core.commands.consumer.CommandHandlers;
import org.cresplanex.core.commands.consumer.CommandMessage;
import org.cresplanex.core.commands.consumer.PathVariables;
import org.cresplanex.core.messaging.common.Message;
import org.cresplanex.core.saga.lock.LockTarget;
import org.cresplanex.core.saga.participant.SagaCommandHandlersBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.cresplanex.core.commands.consumer.CommandHandlerReplyBuilder.*;
import static org.cresplanex.core.saga.participant.SagaReplyMessageBuilder.withLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamSagaCommandHandlers {

    private final TeamService teamService;

    public CommandHandlers commandHandlers() {
        return SagaCommandHandlersBuilder
                .fromChannel(SagaCommandChannel.TEAM)
                .onMessage(CreateTeamAndAddInitialTeamUserCommand.Exec.class,
                        CreateTeamAndAddInitialTeamUserCommand.Exec.TYPE,
                        this::handleCreateTeamAndAddInitialTeamUserCommand
                )
                .onMessage(CreateTeamAndAddInitialTeamUserCommand.Undo.class,
                        CreateTeamAndAddInitialTeamUserCommand.Undo.TYPE,
                        this::handleUndoCreateTeamAndAddInitialTeamUserCommand
                )
                .withPreLock(this::undoCreateTeamAndAddInitialTeamUserPreLock)

                .onMessage(CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec.class,
                        CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec.TYPE,
                        this::handleCreateDefaultTeamAndAddInitialDefaultTeamUserCommand
                )
                .onMessage(CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo.class,
                        CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo.TYPE,
                        this::handleUndoCreateDefaultTeamAndAddInitialDefaultTeamUserCommand
                )
                .withPreLock(this::undoCreateDefaultTeamAndAddInitialDefaultTeamUserPreLock)

                .onMessage(AddUsersTeamCommand.Exec.class,
                        AddUsersTeamCommand.Exec.TYPE,
                        this::handleAddUsersTeamCommand
                )
                .onMessage(AddUsersTeamCommand.Undo.class,
                        AddUsersTeamCommand.Undo.TYPE,
                        this::handleUndoAddUsersTeamCommand
                )

                .onMessage(AddUsersDefaultTeamCommand.Exec.class,
                        AddUsersDefaultTeamCommand.Exec.TYPE,
                        this::handleAddUsersDefaultTeamCommand
                )

                .onMessage(AddUsersDefaultTeamCommand.Undo.class,
                        AddUsersDefaultTeamCommand.Undo.TYPE,
                        this::handleUndoAddUsersDefaultTeamCommand
                )

                .onMessage(TeamExistValidateCommand.class,
                        TeamExistValidateCommand.TYPE,
                        this::handleTeamExistValidateCommand
                )
                .build();
    }

    private LockTarget undoCreateTeamAndAddInitialTeamUserPreLock(
            CommandMessage<CreateTeamAndAddInitialTeamUserCommand.Undo> cmd,
            PathVariables pathVariables
    ) {
        return new LockTarget(LockTargetType.TEAM, cmd.getCommand().getTeamId());
    }

    private LockTarget undoCreateDefaultTeamAndAddInitialDefaultTeamUserPreLock(
            CommandMessage<CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo> cmd,
            PathVariables pathVariables
    ) {
        return new LockTarget(LockTargetType.TEAM, cmd.getCommand().getTeamId());
    }

    private Message handleCreateTeamAndAddInitialTeamUserCommand(
            CommandMessage<CreateTeamAndAddInitialTeamUserCommand.Exec> cmd) {
        try {
            CreateTeamAndAddInitialTeamUserCommand.Exec command = cmd.getCommand();
            TeamEntity team = new TeamEntity();
            team.setOrganizationId(command.getOrganizationId());
            team.setName(command.getName());
            team.setDescription(command.getDescription());
            team.setDefault(false);
            List<TeamUserEntity> users = command.getUsers().stream().map(user -> {
                TeamUserEntity userEntity = new TeamUserEntity();
                userEntity.setUserId(user.getUserId());
                return userEntity;
            }).toList();
            team.setTeamUsers(users);
            team = teamService.createAndAddUsers(command.getOperatorId(), team);
            CreateTeamAndAddInitialTeamUserReply.Success reply = new CreateTeamAndAddInitialTeamUserReply.Success(
                    new CreateTeamAndAddInitialTeamUserReply.Success.Data(
                            DtoMapper.convert(team),
                            DtoMapper.convert(team.getTeamUsers())
                    ),
                    TeamServiceApplicationCode.SUCCESS,
                    "Team created successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            return withLock(LockTargetType.TEAM, team.getTeamId())
                    .withSuccess(reply, CreateTeamAndAddInitialTeamUserReply.Success.TYPE);
        } catch (Exception e) {
            CreateTeamAndAddInitialTeamUserReply.Failure reply = new CreateTeamAndAddInitialTeamUserReply.Failure(
                    null,
                    TeamServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to create team",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, CreateTeamAndAddInitialTeamUserReply.Failure.TYPE);
        }
    }

    private Message handleCreateDefaultTeamAndAddInitialDefaultTeamUserCommand(
            CommandMessage<CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec> cmd
    ) {
        try {
            CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Exec command = cmd.getCommand();
            TeamEntity team = new TeamEntity();
            team.setOrganizationId(command.getOrganizationId());
            team.setName(ReservedTeamName.DEFAULT);
            team.setDescription(String.format("Default team for organization %s", command.getOrganizationName()));
            team.setDefault(true);
            List<TeamUserEntity> users = command.getUsers().stream().map(user -> {
                TeamUserEntity userEntity = new TeamUserEntity();
                userEntity.setUserId(user.getUserId());
                return userEntity;
            }).toList();
            team.setTeamUsers(users);
            team = teamService.createAndAddUsers(command.getOperatorId(), team);
            CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Success reply = new CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Success(
                    new CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Success.Data(
                            DtoMapper.convert(team),
                            DtoMapper.convert(team.getTeamUsers())
                    ),
                    TeamServiceApplicationCode.SUCCESS,
                    "Default team created successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            return withLock(LockTargetType.TEAM, team.getTeamId())
                    .withSuccess(reply, CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Success.TYPE);
        } catch (Exception e) {
            CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Failure reply = new CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Failure(
                    null,
                    TeamServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to create default team",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, CreateDefaultTeamAndAddInitialDefaultTeamUserReply.Failure.TYPE);
        }
    }

    private Message handleUndoCreateTeamAndAddInitialTeamUserCommand(
            CommandMessage<CreateTeamAndAddInitialTeamUserCommand.Undo> cmd
    ) {
        try {
        CreateTeamAndAddInitialTeamUserCommand.Undo command = cmd.getCommand();
            String teamId = command.getTeamId();
            teamService.undoCreate(teamId);
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }

    private Message handleUndoCreateDefaultTeamAndAddInitialDefaultTeamUserCommand(
            CommandMessage<CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo> cmd
    ) {
        try {
            CreateDefaultTeamAndAddInitialDefaultTeamUserCommand.Undo command = cmd.getCommand();
            String teamId = command.getTeamId();
            teamService.undoCreate(teamId);
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }

    private Message handleAddUsersTeamCommand(
            CommandMessage<AddUsersTeamCommand.Exec> cmd
    ) {
        try {
            AddUsersTeamCommand.Exec command = cmd.getCommand();
            List<String> users = command.getUsers().stream().map(AddUsersTeamCommand.Exec.User::getUserId).toList();

            List<TeamUserEntity> teamUsers = teamService.addUsers(command.getOperatorId(), command.getTeamId(), users);
            AddUsersTeamReply.Success reply = new AddUsersTeamReply.Success(
                    new AddUsersTeamReply.Success.Data(
                            DtoMapper.convert(teamUsers)
                    ),
                    TeamServiceApplicationCode.SUCCESS,
                    "Users added successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withSuccess(reply, AddUsersTeamReply.Success.TYPE);
        } catch (AlreadyExistTeamUserException e) {
            AddUsersTeamReply.Failure reply = new AddUsersTeamReply.Failure(
                    new AddUsersTeamReply.Failure.AlreadyAddedTeamUser(e.getUserIds()),
                    TeamServiceApplicationCode.ALREADY_EXIST_USER,
                    "Users already added",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, AddUsersTeamReply.Failure.TYPE);
        } catch (Exception e) {
            AddUsersTeamReply.Failure reply = new AddUsersTeamReply.Failure(
                    null,
                    TeamServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to add users",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, AddUsersTeamReply.Failure.TYPE);
        }
    }

    private Message handleAddUsersDefaultTeamCommand(
            CommandMessage<AddUsersDefaultTeamCommand.Exec> cmd
    ) {
        try {
            AddUsersDefaultTeamCommand.Exec command = cmd.getCommand();
            List<String> users = command.getUsers().stream().map(AddUsersDefaultTeamCommand.Exec.User::getUserId).toList();

            List<TeamUserEntity> teamUsers = teamService.addUsersToDefault(command.getOperatorId(), command.getOrganizationId(), users);
            AddUsersDefaultTeamReply.Success reply = new AddUsersDefaultTeamReply.Success(
                    new AddUsersDefaultTeamReply.Success.Data(
                            DtoMapper.convert(teamUsers)
                    ),
                    TeamServiceApplicationCode.SUCCESS,
                    "Users added successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withSuccess(reply, AddUsersDefaultTeamReply.Success.TYPE);
        } catch (AlreadyExistTeamUserException e) {
            AddUsersDefaultTeamReply.Failure reply = new AddUsersDefaultTeamReply.Failure(
                    new AddUsersDefaultTeamReply.Failure.AlreadyAddedTeamUser(e.getUserIds()),
                    TeamServiceApplicationCode.ALREADY_EXIST_USER,
                    "Users already added",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, AddUsersDefaultTeamReply.Failure.TYPE);
        } catch (Exception e) {
            AddUsersDefaultTeamReply.Failure reply = new AddUsersDefaultTeamReply.Failure(
                    null,
                    TeamServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to add users",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, AddUsersDefaultTeamReply.Failure.TYPE);
        }
    }

    private Message handleUndoAddUsersTeamCommand(CommandMessage<AddUsersTeamCommand.Undo> cmd) {
        try {
            AddUsersTeamCommand.Undo command = cmd.getCommand();
            teamService.undoAddUsers(command.getUserTeamIds());
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }

    private Message handleUndoAddUsersDefaultTeamCommand(CommandMessage<AddUsersDefaultTeamCommand.Undo> cmd) {
        try {
            AddUsersDefaultTeamCommand.Undo command = cmd.getCommand();
            teamService.undoAddUsers(command.getUserTeamIds());
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }

    private Message handleTeamExistValidateCommand(
            CommandMessage<TeamExistValidateCommand> cmd
    ) {
        try {
            TeamExistValidateCommand command = cmd.getCommand();
            teamService.validateTeams(command.getTeamIds());
            return withSuccess();
        } catch (NotFoundTeamException e) {
            TeamExistValidateReply.Failure reply = new TeamExistValidateReply.Failure(
                    new TeamExistValidateReply.Failure.TeamNotFound(e.getTeamIds()),
                    TeamServiceApplicationCode.NOT_FOUND,
                    "Team not found",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withFailure(reply, TeamExistValidateReply.Failure.TYPE);
        } catch (Exception e) {
            TeamExistValidateReply.Failure reply =
                    new TeamExistValidateReply.Failure(
                    null,
                    TeamServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to validate team",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withFailure(reply, TeamExistValidateReply.Failure.TYPE);
        }
    }
}
