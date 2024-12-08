package org.cresplanex.api.state.teamservice.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.filter.team.IsDefaultFilter;
import org.cresplanex.api.state.teamservice.filter.team.OrganizationFilter;
import org.cresplanex.api.state.teamservice.filter.team.UsersFilter;
import org.hibernate.type.descriptor.java.BooleanJavaType;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TeamSpecifications {

    public static Specification<TeamEntity> whereTeamIds(Iterable<String> teamIds) {
        List<String> teamIdList = new ArrayList<>();
        teamIds.forEach(teamId -> {
            teamIdList.add(new StringJavaType().wrap(teamId, null));
        });

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, root.get("teamId").in(teamIdList));
            return predicate;
        };
    }

    public static Specification<TeamEntity> fetchTeamUsers() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) {
                return null;
            }
            if (Long.class != query.getResultType()) {
                root.fetch("teamUsers", JoinType.LEFT);
                query.distinct(true);
                return null;
            }

            return null;
        };
    }

    public static Specification<TeamEntity> withIsDefaultFilter(IsDefaultFilter isDefaultFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (isDefaultFilter != null && isDefaultFilter.isValid()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isDefault"), new BooleanJavaType().wrap(isDefaultFilter.isDefault(), null)));
            }
            return predicate;
        };
    }

    public static Specification<TeamEntity> withOrganizationFilter(OrganizationFilter organizationFilter) {
        List<String> organizationList = new ArrayList<>();
        if (organizationFilter != null && organizationFilter.isValid()) {
            organizationFilter.getOrganizationIds().forEach(organization -> {
                organizationList.add(new StringJavaType().wrap(organization, null));
            });
        }

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (organizationFilter != null && organizationFilter.isValid()) {
                predicate = criteriaBuilder.and(predicate, root.get("organizationId").in(organizationList));
            }
            return predicate;
        };
    }

    public static Specification<TeamEntity> withBelongUsersFilter(UsersFilter usersFilter) {
        List<String> userList = new ArrayList<>();
        if (usersFilter != null && usersFilter.isValid()) {
            usersFilter.getUserIds().forEach(user -> {
                userList.add(new StringJavaType().wrap(user, null));
            });
        }

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (usersFilter != null && usersFilter.isValid()) {
                if (!usersFilter.isAny()) {
                    // all
                    for (String userId : userList) {
                        predicate = criteriaBuilder.and(predicate, criteriaBuilder.isMember(userId, root.get("teamUsers")));
                    }
                } else {
                    // any
                    predicate = criteriaBuilder.and(predicate, root.get("teamUsers").get("userId").in(userList));
                }
            }
            return predicate;
        };
    }
}
