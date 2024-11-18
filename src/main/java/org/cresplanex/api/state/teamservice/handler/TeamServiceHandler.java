package org.cresplanex.api.state.teamservice.handler;

import build.buf.gen.team.v1.*;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.cresplanex.api.state.teamservice.mapper.proto.ProtoMapper;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.cresplanex.api.state.teamservice.service.TeamService;

import java.util.List;

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

    // TODO: pagination + with count
    @Override
    public void getTeams(GetTeamsRequest request, StreamObserver<GetTeamsResponse> responseObserver) {
        List<TeamEntity> teams = teamService.get();

        List<Team> teamProtos = teams.stream()
                .map(ProtoMapper::convert).toList();
        GetTeamsResponse response = GetTeamsResponse.newBuilder()
                .addAllTeams(teamProtos)
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
