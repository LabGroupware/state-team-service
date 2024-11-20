package org.cresplanex.api.state.teamservice.filter.team;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrganizationFilter {

    private boolean isValid;
    private List<String> organizationIds;
}
