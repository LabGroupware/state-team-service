package org.cresplanex.api.state.teamservice.mapper.dto;

import org.cresplanex.api.state.common.dto.team.TeamDto;
import org.cresplanex.api.state.common.dto.team.TeamWithUsersDto;
import org.cresplanex.api.state.common.dto.team.UserOnTeamDto;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;

import java.util.List;

public class DtoMapper {

    public static TeamDto convert(TeamEntity teamEntity) {
        return TeamDto.builder()
                .teamId(teamEntity.getTeamId())
                .organizationId(teamEntity.getOrganizationId())
                .name(teamEntity.getName())
                .description(teamEntity.getDescription())
                .isDefault(teamEntity.isDefault())
                .build();
    }

    public static UserOnTeamDto convert(TeamUserEntity teamUserEntity) {
        return UserOnTeamDto.builder()
                .userTeamId(teamUserEntity.getTeamUserId())
                .userTeamId(teamUserEntity.getTeamId())
                .userId(teamUserEntity.getUserId())
                .build();
    }

    public static List<UserOnTeamDto> convert(List<TeamUserEntity> teamUserEntities) {
        return teamUserEntities.stream()
                .map(DtoMapper::convert)
                .toList();
    }

    public static TeamWithUsersDto convert(TeamEntity teamEntity, List<TeamUserEntity> teamUserEntities) {
        return TeamWithUsersDto.builder()
                .team(DtoMapper.convert(teamEntity))
                .users(convert(teamUserEntities))
                .build();
    }
}
