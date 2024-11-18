package org.cresplanex.api.state.teamservice.mapper.proto;

import build.buf.gen.team.v1.Team;
import build.buf.gen.team.v1.TeamWithUsers;
import build.buf.gen.team.v1.UserOnTeam;
import org.cresplanex.api.state.common.utils.ValueFromNullable;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;

import java.util.List;

public class ProtoMapper {

    public static Team convert(TeamEntity teamEntity) {

        return Team.newBuilder()
                .setTeamId(teamEntity.getTeamId())
                .setOrganizationId(teamEntity.getOrganizationId())
                .setName(teamEntity.getName())
                .setDescription(ValueFromNullable.toNullableString(teamEntity.getDescription()))
                .setIsDefault(teamEntity.isDefault())
                .build();
    }

    public static UserOnTeam convert(TeamUserEntity userOnTeamEntity) {
        return UserOnTeam.newBuilder()
                .setUserId(userOnTeamEntity.getUserId())
                .build();
    }

    public static List<UserOnTeam> convert(List<TeamUserEntity> userOnTeamEntities) {
        return userOnTeamEntities.stream()
                .map(ProtoMapper::convert)
                .toList();
    }

    public static TeamWithUsers convert(TeamEntity teamEntity, List<TeamUserEntity> userOnTeamEntities) {
        return TeamWithUsers.newBuilder()
                .setTeam(convert(teamEntity))
                .addAllUsers(convert(userOnTeamEntities))
                .build();
    }
}
