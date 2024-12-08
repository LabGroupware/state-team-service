package org.cresplanex.api.state.teamservice.repository;

import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
