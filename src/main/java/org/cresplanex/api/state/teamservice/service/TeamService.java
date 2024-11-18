package org.cresplanex.api.state.teamservice.service;

import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.saga.local.organization.InvalidOrganizationPlanException;
import org.cresplanex.api.state.common.saga.local.organization.NotFoundOrganizationException;
import org.cresplanex.api.state.common.saga.local.team.AlreadyExistTeamNameInOrganizationException;
import org.cresplanex.api.state.common.saga.local.team.NotAllowedOnDefaultTeamException;
import org.cresplanex.api.state.common.saga.local.team.NotFoundTeamException;
import org.cresplanex.api.state.common.saga.local.team.ReservedTeamNameException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.teamservice.constants.ActionOnTeam;
import org.cresplanex.api.state.teamservice.constants.ReservedTeamName;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.cresplanex.api.state.teamservice.exception.AlreadyExistTeamUserException;
import org.cresplanex.api.state.teamservice.exception.NotFoundTeamUserException;
import org.cresplanex.api.state.teamservice.exception.TeamNotFoundException;
import org.cresplanex.api.state.teamservice.repository.TeamRepository;
import org.cresplanex.api.state.teamservice.repository.TeamUserRepository;
import org.cresplanex.api.state.teamservice.saga.model.team.AddUsersTeamSaga;
import org.cresplanex.api.state.teamservice.saga.model.team.CreateTeamSaga;
import org.cresplanex.api.state.teamservice.saga.state.team.AddUsersTeamSagaState;
import org.cresplanex.api.state.teamservice.saga.state.team.CreateTeamSagaState;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TeamService extends BaseService {

    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;
    private final SagaInstanceFactory sagaInstanceFactory;

    private final CreateTeamSaga createTeamSaga;
    private final AddUsersTeamSaga addUsersTeamSaga;

    @Transactional(readOnly = true)
    public TeamEntity findById(String organizationId) {
        return internalFindById(organizationId);
    }

    private TeamEntity internalFindById(String organizationId) {
        return teamRepository.findById(organizationId).orElseThrow(() -> new TeamNotFoundException(
                TeamNotFoundException.FindType.BY_ID,
                organizationId
        ));
    }

    @Transactional(readOnly = true)
    public List<TeamEntity> get() {
        return teamRepository.findAll();
    }

    @Transactional
    public String beginCreate(String operatorId, TeamEntity team, List<TeamUserEntity> users) {
        CreateTeamSagaState.InitialData initialData = CreateTeamSagaState.InitialData.builder()
                .name(team.getName())
                .description(team.getDescription())
                .organizationId(team.getOrganizationId())
                .users(users.stream().map(user -> CreateTeamSagaState.InitialData.User.builder()
                        .userId(user.getUserId())
                        .build())
                        .toList())
                .build();
        CreateTeamSagaState state = new CreateTeamSagaState();
        state.setInitialData(initialData);
        state.setOperatorId(operatorId);

        String jobId = getJobId();
        state.setJobId(jobId);

        try {
            sagaInstanceFactory.create(createTeamSaga, state);
        } catch (LocalException e) {
            // Jobで失敗イベント送信済みのため, ここでは何もしない
            log.debug("LocalException: {}", e.getMessage());
            return jobId;
        }

        return jobId;
    }

    @Transactional
    public String beginAddUsers(String operatorId, String teamId, List<TeamUserEntity> users) {
        AddUsersTeamSagaState.InitialData initialData = AddUsersTeamSagaState.InitialData.builder()
                .teamId(teamId)
                .users(users.stream().map(user -> AddUsersTeamSagaState.InitialData.User.builder()
                        .userId(user.getUserId())
                        .build())
                        .toList())
                .build();
        AddUsersTeamSagaState state = new AddUsersTeamSagaState();
        state.setInitialData(initialData);
        state.setOperatorId(operatorId);

        String jobId = getJobId();
        state.setJobId(jobId);

        try {
            sagaInstanceFactory.create(addUsersTeamSaga, state);
        } catch (LocalException e) {
            // Jobで失敗イベント送信済みのため, ここでは何もしない
            log.debug("LocalException: {}", e.getMessage());
            return jobId;
        }

        return jobId;
    }

    public void validateCreatedTeam(String organizationId, String name, String description)
            throws AlreadyExistTeamNameInOrganizationException, ReservedTeamNameException {
        if (Arrays.asList(ReservedTeamName.ALL).contains(name)) {
            throw new ReservedTeamNameException(List.of(name));
        }
        teamRepository.findByOrganizationIdAndName(organizationId, name)
                .ifPresent(organization -> {
                    throw new AlreadyExistTeamNameInOrganizationException(organizationId, List.of(name));
                });
    }

    public void validateTeams(List<String> teamIds)
            throws NotFoundTeamException {
        List<TeamEntity> teams =  teamRepository.findAllById(teamIds);
        if (teams.size() != teamIds.size()) {
            List<String> notFoundTeamIds = new ArrayList<>();
            teamIds.stream()
                    .filter(teamId -> teams.stream().noneMatch(team -> team.getTeamId().equals(teamId)))
                    .forEach(notFoundTeamIds::add);

            throw new NotFoundTeamException(notFoundTeamIds);
        }
    }

    public TeamEntity validateTeam(String teamId, String actionType)
            throws NotFoundTeamException {
        TeamEntity teamEntity = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundTeamException(List.of(teamId)));
        if (teamEntity.isDefault()){
            switch (actionType) {
                case ActionOnTeam.UPDATE_PROFILE:
                    throw new NotAllowedOnDefaultTeamException(List.of(teamId), ActionOnTeam.UPDATE_PROFILE);
                case ActionOnTeam.DELETE:
                    throw new NotAllowedOnDefaultTeamException(List.of(teamId), ActionOnTeam.DELETE);
                case ActionOnTeam.ADD_USERS:
                    throw new NotAllowedOnDefaultTeamException(List.of(teamId), ActionOnTeam.ADD_USERS);
                case ActionOnTeam.REMOVE_USERS:
                    throw new NotAllowedOnDefaultTeamException(List.of(teamId), ActionOnTeam.REMOVE_USERS);
            }
        }

        return teamEntity;
    }

    public TeamEntity createAndAddUsers(String operatorId, TeamEntity organization) {
        return teamRepository.save(organization);
    }

    public List<TeamUserEntity> addUsers(String operatorId, String teamId, List<String> userIds) {
        List<TeamUserEntity> existUsers = teamUserRepository.
                findAllByTeamIdAndUserIds(teamId, userIds);
        if (!existUsers.isEmpty()) {
            List<String> existUserIds = existUsers.stream()
                    .map(TeamUserEntity::getUserId)
                    .toList();
            throw new AlreadyExistTeamUserException(teamId, existUserIds);
        }
        List<TeamUserEntity> users = userIds.stream()
                .map(userId -> {
                    TeamUserEntity user = new TeamUserEntity();
                    user.setTeamId(teamId);
                    user.setUserId(userId);
                    return user;
                })
                .toList();
        return teamUserRepository.saveAll(users);
    }

    public List<TeamUserEntity> addUsersToDefault(String operatorId, String organizationId, List<String> userIds) {
        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndIsDefault(organizationId, true).orElseThrow(() -> new TeamNotFoundException(
                TeamNotFoundException.FindType.BY_ORGANIZATION_ID_AND_IS_DEFAULT,
                organizationId
        ));
        return addUsers(operatorId, teamEntity.getTeamId(), userIds);
    }

    public void undoCreate(String teamId) {
        TeamEntity team = teamRepository.findByIdWithUsers(teamId)
                .orElseThrow(() -> new TeamNotFoundException(
                        TeamNotFoundException.FindType.BY_ID,
                        teamId
                ));
        teamRepository.delete(team);
    }

    public void undoAddUsers(List<String> organizationUserIds) {
        teamUserRepository.deleteAllById(organizationUserIds);
    }
}
