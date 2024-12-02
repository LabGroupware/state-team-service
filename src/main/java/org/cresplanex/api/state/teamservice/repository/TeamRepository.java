package org.cresplanex.api.state.teamservice.repository;

import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.enums.TeamSortType;
import org.cresplanex.api.state.teamservice.enums.TeamWithUsersSortType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, String>, JpaSpecificationExecutor<TeamEntity> {

    /**
     * Teamを取得し、TeamUserをJOINした状態で取得。
     *
     * @param teamId チームID
     * @return Teamオプショナルオブジェクト
     */
    @Query("SELECT o FROM TeamEntity o LEFT JOIN FETCH o.teamUsers WHERE o.teamId = :teamId")
    Optional<TeamEntity> findByIdWithUsers(String teamId);

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

    @Query("SELECT o FROM TeamEntity o")
    List<TeamEntity> findList(Specification<TeamEntity> specification, Pageable pageable);

    @Query("SELECT o FROM TeamEntity o LEFT JOIN FETCH o.teamUsers")
    List<TeamEntity> findListWithUsers(Specification<TeamEntity> specification, Pageable pageable);

    @Query("SELECT COUNT(o) FROM TeamEntity o")
    int countList(Specification<TeamEntity> specification);
}
