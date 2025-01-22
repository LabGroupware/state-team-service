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
import org.cresplanex.api.state.teamservice.specification.TeamUserSpecifications;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<TeamEntity> data = teamRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int) data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
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
//                        .and(TeamSpecifications.fetchTeamUsers()));

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<TeamEntity> data = teamRepository.findAll(spec, pageable);

        List<String> teamIds = data.getContent().stream()
                .map(TeamEntity::getTeamId)
                .toList();

        Specification<TeamUserEntity> userSpec = Specification.where(
                TeamUserSpecifications.whereTeamIds(teamIds));

        List<TeamUserEntity> users = teamUserRepository.findAll(userSpec);

        Map<String, List<TeamUserEntity>> userMap = new HashMap<>();

        users.forEach(user -> {
            if (userMap.containsKey(user.getTeamId())) {
                userMap.get(user.getTeamId()).add(user);
            } else {
                List<TeamUserEntity> list = new ArrayList<>();
                list.add(user);
                userMap.put(user.getTeamId(), list);
            }
        });

        data.getContent().forEach(team -> {
            team.setTeamUsers(userMap.get(team.getTeamId()));
        });


        int count = 0;
        if (withCount){
            count = (int) data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
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
        Specification<TeamUserEntity> spec = Specification
                .where(TeamUserSpecifications.whereTeamId(teamId));

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<TeamUserEntity> data = teamUserRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int) data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
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
        Specification<TeamUserEntity> spec = Specification
                .where(TeamUserSpecifications.whereUserId(userId)
                        .and(TeamUserSpecifications.fetchTeam()));

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<TeamUserEntity> data = teamUserRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int) data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
                count
        );
    }

    @Transactional(readOnly = true)
    public List<TeamEntity> getByTeamIds(
            List<String> teamIds,
            TeamSortType sortType
    ) {
        Specification<TeamEntity> spec = Specification.where(
                TeamSpecifications.whereTeamIds(teamIds)
        );

        return teamRepository.findAll(spec, createSort(sortType));
    }

    @Transactional(readOnly = true)
    public List<TeamEntity> getByTeamIdsWithUsers(
            List<String> teamIds,
            TeamWithUsersSortType sortType
    ) {
        Specification<TeamEntity> spec = Specification.where(
                TeamSpecifications.whereTeamIds(teamIds)
        ).and(TeamSpecifications.fetchTeamUsers());

        return teamRepository.findAll(spec, createSort(sortType));
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
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(
                        TeamNotFoundException.FindType.BY_ID,
                        teamId
                ));
        users = users.stream()
                .peek(user -> user.setTeam(team))
                .toList();
        return teamUserRepository.saveAll(users);
    }

    public List<TeamUserEntity> addUsersToDefault(String operatorId, String organizationId, List<TeamUserEntity> users) {
        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndIsDefault(organizationId, true).orElseThrow(() -> new TeamNotFoundException(
                TeamNotFoundException.FindType.BY_ORGANIZATION_ID_AND_IS_DEFAULT,
                organizationId
        ));
        List<TeamUserEntity> existUsers = teamUserRepository.
                findAllByTeamIdAndUserIds(teamEntity.getTeamId(), users.stream()
                        .map(TeamUserEntity::getUserId)
                        .toList());
        if (!existUsers.isEmpty()) {
            List<String> existUserIds = existUsers.stream()
                    .map(TeamUserEntity::getUserId)
                    .toList();
            throw new AlreadyExistTeamUserException(teamEntity.getTeamId(), existUserIds);
        }
        users = users.stream()
                .peek(user -> user.setTeam(teamEntity))
                .toList();
        return teamUserRepository.saveAll(users);
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

    private Sort createSort(TeamSortType sortType) {
        return switch (sortType) {
            case CREATED_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
            case NAME_ASC -> Sort.by(Sort.Order.asc("name"), Sort.Order.desc("createdAt"));
            case NAME_DESC -> Sort.by(Sort.Order.desc("name"), Sort.Order.desc("createdAt"));
        };
    }

    private Sort createSort(TeamWithUsersSortType sortType) {
        return switch (sortType) {
            case CREATED_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
            case NAME_ASC -> Sort.by(Sort.Order.asc("name"), Sort.Order.desc("createdAt"));
            case NAME_DESC -> Sort.by(Sort.Order.desc("name"), Sort.Order.desc("createdAt"));
        };
    }

    private Sort createSort(UserOnTeamSortType sortType) {
        return switch (sortType) {
            case ADD_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case ADD_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
        };
    }

    private Sort createSort(TeamOnUserSortType sortType) {
        return switch (sortType) {
            case ADD_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case ADD_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
            case NAME_ASC -> Sort.by(Sort.Order.asc("team.name"), Sort.Order.desc("createdAt"));
            case NAME_DESC -> Sort.by(Sort.Order.desc("team.name"), Sort.Order.desc("createdAt"));
            case CREATED_AT_ASC -> Sort.by(Sort.Order.asc("team.createdAt"), Sort.Order.desc("createdAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("team.createdAt"), Sort.Order.desc("createdAt"));
        };
    }
}
