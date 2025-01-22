package org.cresplanex.api.state.teamservice.handler;

import build.buf.gen.cresplanex.nova.v1.Count;
import build.buf.gen.cresplanex.nova.v1.SortOrder;
import build.buf.gen.team.v1.*;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.entity.ListEntityWithCount;
import org.cresplanex.api.state.common.enums.PaginationType;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.cresplanex.api.state.teamservice.enums.TeamOnUserSortType;
import org.cresplanex.api.state.teamservice.enums.TeamSortType;
import org.cresplanex.api.state.teamservice.enums.TeamWithUsersSortType;
import org.cresplanex.api.state.teamservice.enums.UserOnTeamSortType;
import org.cresplanex.api.state.teamservice.filter.team.IsDefaultFilter;
import org.cresplanex.api.state.teamservice.filter.team.OrganizationFilter;
import org.cresplanex.api.state.teamservice.filter.team.UsersFilter;
import org.cresplanex.api.state.teamservice.mapper.proto.ProtoMapper;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.cresplanex.api.state.teamservice.service.TeamService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class TeamServiceHandler extends TeamServiceGrpc.TeamServiceImplBase {

    private final TeamService teamService;

    @Override
    public void findTeam(FindTeamRequest request, StreamObserver<FindTeamResponse> responseObserver) {
        TeamEntity team = teamService.findById(request.getTeamId());

        Team teamProto = ProtoMapper.convert(team);
        FindTeamResponse response = FindTeamResponse.newBuilder()
                .setTeam(teamProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void findTeamWithUsers(FindTeamWithUsersRequest request, StreamObserver<FindTeamWithUsersResponse> responseObserver) {
        TeamEntity team = teamService.findByIdWithUsers(request.getTeamId());

        TeamWithUsers teamProto = ProtoMapper.convertWithUsers(team);
        FindTeamWithUsersResponse response = FindTeamWithUsersResponse.newBuilder()
                .setTeam(teamProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTeams(GetTeamsRequest request, StreamObserver<GetTeamsResponse> responseObserver) {
        TeamSortType sortType = switch (request.getSort().getOrderField()) {
            case TEAM_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamSortType.NAME_ASC : TeamSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamSortType.CREATED_AT_ASC : TeamSortType.CREATED_AT_DESC;
        };
        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        IsDefaultFilter isDefaultFilter = new IsDefaultFilter(
                request.getFilterIsDefault().getHasValue(), request.getFilterIsDefault().getIsDefault()
        );

        OrganizationFilter organizationFilter = new OrganizationFilter(
                request.getFilterOrganization().getHasValue(), request.getFilterOrganization().getOrganizationIdsList()
        );

        UsersFilter usersFilter = new UsersFilter(
                request.getFilterUser().getHasValue(), request.getFilterUser().getAny(), request.getFilterUser().getUserIdsList()
        );

        ListEntityWithCount<TeamEntity> organizations = teamService.get(
                paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount(), isDefaultFilter, organizationFilter, usersFilter);

        List<Team> organizationProtos = organizations.getData().stream()
                .map(ProtoMapper::convert).toList();
        GetTeamsResponse response = GetTeamsResponse.newBuilder()
                .addAllTeams(organizationProtos)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(organizations.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTeamsWithUsers(GetTeamsWithUsersRequest request, StreamObserver<GetTeamsWithUsersResponse> responseObserver) {
        TeamWithUsersSortType sortType = switch (request.getSort().getOrderField()) {
            case TEAM_WITH_USERS_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamWithUsersSortType.NAME_ASC : TeamWithUsersSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamWithUsersSortType.CREATED_AT_ASC : TeamWithUsersSortType.CREATED_AT_DESC;
        };
        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        IsDefaultFilter isDefaultFilter = new IsDefaultFilter(
                request.getFilterIsDefault().getHasValue(), request.getFilterIsDefault().getIsDefault()
        );

        OrganizationFilter organizationFilter = new OrganizationFilter(
                request.getFilterOrganization().getHasValue(), request.getFilterOrganization().getOrganizationIdsList()
        );

        UsersFilter usersFilter = new UsersFilter(
                request.getFilterUser().getHasValue(), request.getFilterUser().getAny(), request.getFilterUser().getUserIdsList()
        );

        ListEntityWithCount<TeamEntity> teams = teamService.getWithUsers(
                paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount(), isDefaultFilter, organizationFilter, usersFilter);

        List<TeamWithUsers> organizationProtos = teams.getData().stream()
                .map(ProtoMapper::convertWithUsers).toList();
        GetTeamsWithUsersResponse response = GetTeamsWithUsersResponse.newBuilder()
                .addAllTeams(organizationProtos)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(teams.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPluralTeams(GetPluralTeamsRequest request, StreamObserver<GetPluralTeamsResponse> responseObserver) {
        TeamSortType sortType = switch (request.getSort().getOrderField()) {
            case TEAM_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamSortType.NAME_ASC : TeamSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamSortType.CREATED_AT_ASC : TeamSortType.CREATED_AT_DESC;
        };
        List<Team> organizationProtos = this.teamService.getByTeamIds(
                        request.getTeamIdsList(), sortType).stream()
                .map(ProtoMapper::convert).toList();
        GetPluralTeamsResponse response = GetPluralTeamsResponse.newBuilder()
                .addAllTeams(organizationProtos)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void getPluralTeamsWithUsers(GetPluralTeamsWithUsersRequest request, StreamObserver<GetPluralTeamsWithUsersResponse> responseObserver) {
        TeamWithUsersSortType sortType = switch (request.getSort().getOrderField()) {
            case TEAM_WITH_USERS_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamWithUsersSortType.NAME_ASC : TeamWithUsersSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamWithUsersSortType.CREATED_AT_ASC : TeamWithUsersSortType.CREATED_AT_DESC;
        };
        List<TeamWithUsers> organizationProtos = this.teamService.getByTeamIdsWithUsers(
                        request.getTeamIdsList(), sortType).stream()
                .map(ProtoMapper::convertWithUsers).toList();
        GetPluralTeamsWithUsersResponse response = GetPluralTeamsWithUsersResponse.newBuilder()
                .addAllTeams(organizationProtos)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUsersOnTeam(GetUsersOnTeamRequest request, StreamObserver<GetUsersOnTeamResponse> responseObserver) {
        UserOnTeamSortType sortType = switch (request.getSort().getOrderField()) {
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    UserOnTeamSortType.ADD_AT_ASC : UserOnTeamSortType.ADD_AT_DESC;
        };
        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        ListEntityWithCount<TeamUserEntity> organizations = teamService.getUsersOnTeam(
                request.getTeamId(), paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount());

        List<UserOnTeam> userOnTeams = organizations.getData().stream()
                .map(ProtoMapper::convert).toList();

        GetUsersOnTeamResponse response = GetUsersOnTeamResponse.newBuilder()
                .addAllUsers(userOnTeams)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(organizations.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTeamsOnUser(GetTeamsOnUserRequest request, StreamObserver<GetTeamsOnUserResponse> responseObserver) {

        TeamOnUserSortType sortType = switch (request.getSort().getOrderField()) {
            case TEAM_ON_USER_ORDER_FIELD_CREATE -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamOnUserSortType.CREATED_AT_ASC : TeamOnUserSortType.CREATED_AT_DESC;
            case TEAM_ON_USER_ORDER_FIELD_NAME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamOnUserSortType.NAME_ASC : TeamOnUserSortType.NAME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TeamOnUserSortType.ADD_AT_ASC : TeamOnUserSortType.ADD_AT_DESC;
        };

        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        ListEntityWithCount<TeamUserEntity> organizations = teamService.getTeamsOnUser(
                request.getUserId(), paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount());

        List<TeamOnUser> organizationOnUsers = organizations.getData().stream()
                .map(ProtoMapper::convertOnUser).toList();

        GetTeamsOnUserResponse response = GetTeamsOnUserResponse.newBuilder()
                .addAllTeams(organizationOnUsers)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(organizations.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createTeam(CreateTeamRequest request, StreamObserver<CreateTeamResponse> responseObserver) {
        String operatorId = request.getOperatorId();
        TeamEntity team = new TeamEntity();
        team.setName(request.getName());
        team.setDescription(request.getDescription().getHasValue() ? request.getDescription().getValue() : null);
        team.setOrganizationId(request.getOrganizationId());
        List<TeamUserEntity> users = request.getUsersList().stream()
                .map(user -> {
                    TeamUserEntity userEntity = new TeamUserEntity();
                    userEntity.setUserId(user.getUserId());
                    return userEntity;
                })
                .toList();

        String jobId = teamService.beginCreate(operatorId, team, users);
        CreateTeamResponse response = CreateTeamResponse.newBuilder()
                .setJobId(jobId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addTeamUser(AddTeamUserRequest request, StreamObserver<AddTeamUserResponse> responseObserver) {
        String operatorId = request.getOperatorId();
        String teamId = request.getTeamId();
        List<TeamUserEntity> users = request.getUsersList().stream()
                .map(user -> {
                    TeamUserEntity userEntity = new TeamUserEntity();
                    userEntity.setUserId(user.getUserId());
                    return userEntity;
                })
                .toList();

        String jobId = teamService.beginAddUsers(operatorId, teamId, users);
        AddTeamUserResponse response = AddTeamUserResponse.newBuilder()
                .setJobId(jobId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
