package org.cresplanex.api.state.teamservice.specification;

import jakarta.persistence.criteria.Predicate;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.filter.team.IsDefaultFilter;
import org.cresplanex.api.state.teamservice.filter.team.OrganizationFilter;
import org.cresplanex.api.state.teamservice.filter.team.UsersFilter;
import org.springframework.data.jpa.domain.Specification;

public class TeamSpecifications {

    public static Specification<TeamEntity> withIsDefaultFilter(IsDefaultFilter isDefaultFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (isDefaultFilter != null && isDefaultFilter.isValid()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isDefault"), isDefaultFilter.isDefault()));
            }
            return predicate;
        };
    }

    public static Specification<TeamEntity> withOrganizationFilter(OrganizationFilter organizationFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (organizationFilter != null && organizationFilter.isValid()) {
                if (organizationFilter.getOrganizationIds() != null && !organizationFilter.getOrganizationIds().isEmpty()) {
                    predicate = criteriaBuilder.and(predicate, root.get("organizationId").in(organizationFilter.getOrganizationIds()));
                }
            }
            return predicate;
        };
    }

    public static Specification<TeamEntity> withBelongUsersFilter(UsersFilter usersFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (usersFilter != null && usersFilter.isValid()) {
                if (usersFilter.getUserIds() != null && !usersFilter.getUserIds().isEmpty()) {
                    if (!usersFilter.isAny()) {
                        // all
                        for (String userId : usersFilter.getUserIds()) {
                            predicate = criteriaBuilder.and(predicate, criteriaBuilder.isMember(userId, root.get("teamUsers")));
                        }
                    } else {
                        // any
                        predicate = criteriaBuilder.and(predicate, root.get("teamUsers").get("userId").in(usersFilter.getUserIds()));
                    }
                }
            }
            return predicate;
        };
    }
}
