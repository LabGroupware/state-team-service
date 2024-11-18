package org.cresplanex.api.state.teamservice.repository;

import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamUserRepository extends JpaRepository<TeamUserEntity, String> {

    /**
     * 特定のteamIdに紐づくTeamUserEntityのリストを取得。
     *
     * @param teamId チームID
     * @return TeamUserEntityのリスト
     */
    List<TeamUserEntity> findAllByTeamId(String teamId);

    /**
     * 特定のuserIdに紐づくTeamUserEntityのリストを取得。
     *
     * @param userId ユーザーID
     * @return TeamUserEntityのリスト
     */
    List<TeamUserEntity> findAllByUserId(String userId);

    /**
     * TeamUserEntityを取得し、TeamをJOINした状態で取得。
     *
     * @param teamUserId チームユーザーID
     * @return TeamUserEntityオプショナルオブジェクト
     */
    @Query("SELECT ou FROM TeamUserEntity ou JOIN FETCH ou.team WHERE ou.teamUserId = :teamUserId")
    Optional<TeamUserEntity> findByIdWithTeam(String teamUserId);

    /**
     * 特定のteamIdとuserIdsに紐づくTeamUserEntityのリストを取得。
     *
     * @param teamId チームID
     * @param userIds ユーザーIDリスト
     * @return TeamUserEntityのリスト
     */
    @Query("SELECT ou FROM TeamUserEntity ou WHERE ou.teamId = :teamId AND ou.userId IN :userIds")
    List<TeamUserEntity> findAllByTeamIdAndUserIds(String teamId, List<String> userIds);
}
