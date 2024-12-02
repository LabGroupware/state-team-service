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
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT ou FROM TeamUserEntity ou")
    List<TeamUserEntity> findList(Specification<TeamUserEntity> specification, Pageable pageable);

    @Query("SELECT ou FROM TeamUserEntity ou JOIN FETCH ou.team")
    List<TeamUserEntity> findListWithTeam(Specification<TeamUserEntity> specification, Pageable pageable);

    @Query("SELECT COUNT(ou) FROM TeamUserEntity ou")
    int countList(Specification<TeamUserEntity> specification);
}
