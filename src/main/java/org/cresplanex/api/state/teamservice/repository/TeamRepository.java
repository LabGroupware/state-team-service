package org.cresplanex.api.state.teamservice.repository;

import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, String> {

    /**
     * Teamを取得し、TeamUserをJOINした状態で取得。
     *
     * @param teamId チームID
     * @return Teamオプショナルオブジェクト
     */
    @Query("SELECT o FROM TeamEntity o LEFT JOIN FETCH o.teamUsers WHERE o.teamId = :teamId")
    Optional<TeamEntity> findByIdWithUsers(String teamId);

    /**
     * List<TeamId>を取得し, TeamUserをJOINした状態で取得。
     *
     * @param teamIds チームIDリスト
     * @return TeamEntityのリスト
     */
    @Query("SELECT o FROM TeamEntity o LEFT JOIN FETCH o.teamUsers WHERE o.teamId IN :teamIds")
    List<TeamEntity> findAllByIdWithUsers(List<String> teamIds);

    /**
     * Organization IDでTeamを取得。
     *
     * @param organizationId チームID
     * @return Teamのリスト
     */
    List<TeamEntity> findByOrganizationId(String organizationId);

    /**
     * List<TeamId>の数を取得
     *
     * @param teamIds チームIDリスト
     * @return チームIDの数
     */
    Optional<Long> countByTeamIdIn(List<String> teamIds);

    /**
     * Organization IDとTeam nameでTeamを取得。
     *
     * @param organizationId 組織ID
     * @param name チーム名
     */
    Optional<TeamEntity> findByOrganizationIdAndName(String organizationId, String name);

    /**
     * Organization IDとデフォルトチームのTeamを取得。
     *
     * @param organizationId 組織ID
     * @return TeamEntity
     */
    @Query("SELECT o FROM TeamEntity o WHERE o.organizationId = :organizationId AND o.isDefault = :isDefault")
    Optional<TeamEntity> findByOrganizationIdAndIsDefault(String organizationId, boolean isDefault);
}
