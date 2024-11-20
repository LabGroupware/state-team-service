package org.cresplanex.api.state.teamservice.service;

import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.entity.ListEntityWithCount;
import org.cresplanex.api.state.common.enums.PaginationType;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.saga.local.team.NotFoundTeamException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.cresplanex.api.state.teamservice.enums.TeamOnUserSortType;
import org.cresplanex.api.state.teamservice.enums.TeamSortType;
import org.cresplanex.api.state.teamservice.enums.TeamWithUsersSortType;
import org.cresplanex.api.state.teamservice.enums.UserOnTeamSortType;
import org.cresplanex.api.state.teamservice.exception.AlreadyExistTeamUserException;
import org.cresplanex.api.state.teamservice.exception.TeamNotFoundException;
import org.cresplanex.api.state.teamservice.filter.team.IsDefaultFilter;
import org.cresplanex.api.state.teamservice.filter.team.OrganizationFilter;
import org.cresplanex.api.state.teamservice.filter.team.UsersFilter;
import org.cresplanex.api.state.teamservice.repository.TeamRepository;
import org.cresplanex.api.state.teamservice.repository.TeamUserRepository;
import org.cresplanex.api.state.teamservice.saga.model.team.AddUsersTeamSaga;
import org.cresplanex.api.state.teamservice.saga.model.team.CreateTeamSaga;
import org.cresplanex.api.state.teamservice.saga.state.team.AddUsersTeamSagaState;
import org.cresplanex.api.state.teamservice.saga.state.team.CreateTeamSagaState;
import org.cresplanex.api.state.teamservice.specification.TeamSpecifications;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public TeamEntity findById(String teamId) {
        return internalFindById(teamId);
    }

    @Transactional(readOnly = true)
    public TeamEntity findByIdWithUsers(String teamId) {
        return teamRepository.findByIdWithUsers(teamId).orElseThrow(() -> new TeamNotFoundException(
                TeamNotFoundException.FindType.BY_ID,
                teamId
        ));
    }

    private TeamEntity internalFindById(String teamId) {
        return teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(
                TeamNotFoundException.FindType.BY_ID,
                teamId
        ));
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<TeamEntity> get(
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            TeamSortType sortType,
            boolean withCount,
            IsDefaultFilter isDefaultFilter,
            OrganizationFilter organizationFilter,
            UsersFilter usersFilter
    ) {
        Specification<TeamEntity> spec = Specification.where(
                TeamSpecifications.withIsDefaultFilter(isDefaultFilter)
                        .and(TeamSpecifications.withOrganizationFilter(organizationFilter))
                        .and(TeamSpecifications.withBelongUsersFilter(usersFilter)));

        List<TeamEntity> data = switch (paginationType) {
            case OFFSET ->
                    teamRepository.findList(spec, sortType, PageRequest.of(offset / limit, limit));
            case CURSOR -> teamRepository.findList(spec, sortType); // TODO: Implement cursor pagination
            default -> teamRepository.findList(spec, sortType);
        };

        int count = 0;
        if (withCount){
            count = teamRepository.countList(spec);
        }
        return new ListEntityWithCount<>(
                data,
                count
        );
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<TeamEntity> getWithUsers(
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            TeamWithUsersSortType sortType,
            boolean withCount,
            IsDefaultFilter isDefaultFilter,
            OrganizationFilter organizationFilter,
            UsersFilter usersFilter
    ) {
        Specification<TeamEntity> spec = Specification.where(
                TeamSpecifications.withIsDefaultFilter(isDefaultFilter)
                        .and(TeamSpecifications.withOrganizationFilter(organizationFilter))
                        .and(TeamSpecifications.withBelongUsersFilter(usersFilter)));

        List<TeamEntity> data = switch (paginationType) {
            case OFFSET ->
                    teamRepository.findListWithUsers(spec, sortType, PageRequest.of(offset / limit, limit));
            case CURSOR -> teamRepository.findListWithUsers(spec, sortType); // TODO: Implement cursor pagination
            default -> teamRepository.findListWithUsers(spec, sortType);
        };

        int count = 0;
        if (withCount){
            count = teamRepository.countList(spec);
        }
        return new ListEntityWithCount<>(
                data,
                count
        );
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<TeamUserEntity> getUsersOnTeam(
            String teamId,
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            UserOnTeamSortType sortType,
            boolean withCount
    ) {
        Specification<TeamEntity> spec = Specification.where(null);

        List<TeamUserEntity> data = switch (paginationType) {
            case OFFSET ->
                    teamUserRepository.findUsersListOnTeamWithOffsetPagination(spec, teamId, sortType, PageRequest.of(offset / limit, limit));
            case CURSOR -> teamUserRepository.findUsersListOnTeam(spec, teamId, sortType); // TODO: Implement cursor pagination
            default -> teamUserRepository.findUsersListOnTeam(spec, teamId, sortType);
        };

        int count = 0;
        if (withCount){
            count = teamUserRepository.countUsersListOnTeam(spec, teamId);
        }
        return new ListEntityWithCount<>(
                data,
                count
        );
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<TeamUserEntity> getTeamsOnUser(
            String userId,
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            TeamOnUserSortType sortType,
            boolean withCount
    ) {
        Specification<TeamEntity> spec = Specification.where(null);

        List<TeamUserEntity> data = switch (paginationType) {
            case OFFSET ->
                    teamUserRepository.findTeamsOnUserWithOffsetPagination(spec, userId, sortType, PageRequest.of(offset / limit, limit));
            case CURSOR -> teamUserRepository.findTeamsOnUser(spec, userId, sortType); // TODO: Implement cursor pagination
            default -> teamUserRepository.findTeamsOnUser(spec, userId, sortType);
        };

        int count = 0;
        if (withCount){
            count = teamUserRepository.countTeamsOnUser(spec, userId);
        }
        return new ListEntityWithCount<>(
                data,
                count
        );
    }

    @Transactional(readOnly = true)
    public List<TeamEntity> getByTeamIds(
            List<String> teamIds,
            TeamSortType sortType
    ) {
        return teamRepository.findListByTeamIds(teamIds, sortType);
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

    public TeamEntity createAndAddUsers(String operatorId, TeamEntity team, List<TeamUserEntity> users) {
        team = teamRepository.save(team);
        TeamEntity finalTeam = team;
        users = users.stream()
                .peek(user -> user.setTeam(finalTeam))
                .toList();
        teamUserRepository.saveAll(users);
        team.setTeamUsers(users);
        return team;
    }

    public List<TeamUserEntity> addUsers(String operatorId, String teamId, List<TeamUserEntity> users) {
        List<TeamUserEntity> existUsers = teamUserRepository.
                findAllByTeamIdAndUserIds(teamId, users.stream()
                        .map(TeamUserEntity::getUserId)
                        .toList());
        if (!existUsers.isEmpty()) {
            List<String> existUserIds = existUsers.stream()
                    .map(TeamUserEntity::getUserId)
                    .toList();
            throw new AlreadyExistTeamUserException(teamId, existUserIds);
        }
        return teamUserRepository.saveAll(users);
    }

    public List<TeamUserEntity> addUsersToDefault(String operatorId, String teamId, List<TeamUserEntity> users) {
        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndIsDefault(teamId, true).orElseThrow(() -> new TeamNotFoundException(
                TeamNotFoundException.FindType.BY_ORGANIZATION_ID_AND_IS_DEFAULT,
                teamId
        ));
        users = users.stream()
                .peek(user -> user.setTeam(teamEntity))
                .toList();
        return addUsers(operatorId, teamEntity.getTeamId(), users);
    }

    public void undoCreate(String teamId) {
        TeamEntity team = teamRepository.findByIdWithUsers(teamId)
                .orElseThrow(() -> new TeamNotFoundException(
                        TeamNotFoundException.FindType.BY_ID,
                        teamId
                ));
        teamRepository.delete(team);
    }

    public void undoAddUsers(List<String> teamUserIds) {
        teamUserRepository.deleteAllById(teamUserIds);
    }
}
