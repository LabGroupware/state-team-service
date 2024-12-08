package org.cresplanex.api.state.teamservice.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
public class TeamUserSpecifications {

    public static Specification<TeamUserEntity> fetchTeam() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) {
                return null;
            }
            if (Long.class != query.getResultType()) {
                root.fetch("team", JoinType.LEFT);
                query.distinct(true);
                return null;
            }

            return null;
        };
    }

    public static Specification<TeamUserEntity> whereTeamId(String teamId) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (teamId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("teamId"), new StringJavaType().wrap(teamId, null)));
            }
            return predicate;
        };
    }

    public static Specification<TeamUserEntity> whereUserId(String userId) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (userId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("userId"), new StringJavaType().wrap(userId, null)));
            }
            return predicate;
        };
    }
}
