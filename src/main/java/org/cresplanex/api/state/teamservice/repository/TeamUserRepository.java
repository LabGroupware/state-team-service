package org.cresplanex.api.state.teamservice.repository;

import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.cresplanex.api.state.teamservice.enums.TeamOnUserSortType;
import org.cresplanex.api.state.teamservice.enums.UserOnTeamSortType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamUserRepository extends JpaRepository<TeamUserEntity, String>, JpaSpecificationExecutor<TeamUserEntity> {
    /**
     * 特定のteamIdとuserIdsに紐づくTeamUserEntityのリストを取得。
     *
     * @param teamId チームID
     * @param userIds ユーザーIDリスト
     * @return TeamUserEntityのリスト
     */
    @Query("SELECT ou FROM TeamUserEntity ou WHERE ou.teamId = :teamId AND ou.userId IN :userIds")
    List<TeamUserEntity> findAllByTeamIdAndUserIds(String teamId, List<String> userIds);

    @Query("SELECT ou FROM TeamUserEntity ou WHERE ou.teamId = :teamId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC")
    List<TeamUserEntity> findUsersListOnTeam(Specification<TeamEntity> specification, String teamId, UserOnTeamSortType sortType);

    @Query("SELECT ou FROM TeamUserEntity ou WHERE ou.teamId = :teamId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC")
    List<TeamUserEntity> findUsersListOnTeamWithOffsetPagination(Specification<TeamEntity> specification, String teamId, UserOnTeamSortType sortType, Pageable pageable);

    @Query("SELECT ou FROM TeamUserEntity ou JOIN FETCH ou.team WHERE ou.userId = :userId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC, " +
            "CASE WHEN :sortType = 'NAME_ASC' THEN ou.team.name END ASC, " +
            "CASE WHEN :sortType = 'NAME_DESC' THEN ou.team.name END DESC, " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN ou.team.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN ou.team.createdAt END DESC")
    List<TeamUserEntity> findTeamsOnUser(Specification<TeamEntity> specification, String userId, TeamOnUserSortType sortType);

    @Query("SELECT ou FROM TeamUserEntity ou JOIN FETCH ou.team WHERE ou.userId = :userId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC, " +
            "CASE WHEN :sortType = 'NAME_ASC' THEN ou.team.name END ASC, " +
            "CASE WHEN :sortType = 'NAME_DESC' THEN ou.team.name END DESC, " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN ou.team.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN ou.team.createdAt END DESC")
    List<TeamUserEntity> findTeamsOnUserWithOffsetPagination(Specification<TeamEntity> specification, String userId, TeamOnUserSortType sortType, Pageable pageable);

    @Query("SELECT COUNT(ou) FROM TeamUserEntity ou WHERE ou.teamId = :teamId")
    int countUsersListOnTeam(Specification<TeamEntity> specification, String teamId);

    @Query("SELECT COUNT(ou) FROM TeamUserEntity ou WHERE ou.userId = :userId")
    int countTeamsOnUser(Specification<TeamEntity> specification, String userId);
}
